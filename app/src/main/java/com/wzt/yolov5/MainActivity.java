package com.wzt.yolov5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {
    public static int YOLOV5S = 1;
    public static int USE_MODEL = YOLOV5S;
    public static boolean USE_GPU = false;
    private static final int REQUEST_CAMERA = 1;
    private long startTime = 0;
    private long endTime = 0;
    private int width;
    private int height;
    private int NumOfPeople=0;
    private double threshold = 0.3f;
    private double nms_threshold = 0.7f;
    double total_fps = 0;
    int fps_count = 0;
    private int detection_times=0;//识别次数
    public static int time_interval=1000;//识别时间间隔（毫秒）
    public static String location_id;//位置id

    private int people_sum=0;
    private int request_frequency;
    private static PostRequest_Interface request;

    private Toolbar toolbar;
    private ImageView resultImageView;
    private TextView tvInfo;
    private TextureView viewFinder;

    protected Bitmap mutableBitmap;
    FFmpegMediaMetadataRetriever mmr;
    public static CameraX.LensFacing CAMERA_ID = CameraX.LensFacing.BACK;
    ExecutorService detectService = Executors.newSingleThreadExecutor();
    private static String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private AtomicBoolean detectCamera = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
            finish();
        }
        YOLOv5.init(getAssets(), USE_GPU);

        toolbar = findViewById(R.id.tool_bar);
        resultImageView = findViewById(R.id.imageView);
        tvInfo = findViewById(R.id.tv_info);
        viewFinder = findViewById(R.id.view_finder);

        toolbar.setNavigationIcon(R.drawable.actionbar_dark_back_icon);
        toolbar.setNavigationOnClickListener(v -> finish());

        if(time_interval>10000){
            //如果识别间隔大于10秒，就实时更新
            request_frequency=1;
        }else{
            //如果间隔小于10秒，则识别3次后，再用平均值更新数据库,以减少识别误差
            request_frequency=3;
        }

        //步骤4:创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.3.107:8080/hesuan/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .build();

        // 步骤5:创建 网络请求接口 的实例
        request = retrofit.create(PostRequest_Interface.class);

        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }
    private void updateTransform() {
        Matrix matrix = new Matrix();
        // Compute the center of the view finder
        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        float[] rotations = {0, 90, 180, 270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[viewFinder.getDisplay().getRotation()];

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }
    private void startCamera() {
        CameraX.unbindAll();
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CAMERA_ID)
                //.setTargetAspectRatio(Rational.NEGATIVE_INFINITY)  // 宽高比
                .setTargetResolution(new Size(480, 640))  // 分辨率
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });
        DetectAnalyzer detectAnalyzer = new DetectAnalyzer();
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, gainAnalyzer(detectAnalyzer));
    }

    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        analysisConfigBuilder.setTargetResolution(new Size(480, 640));  // 输出预览图像尺寸
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }
    private class DetectAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            detectOnModel(image, rotationDegrees);
        }
    }

    private void detectOnModel(ImageProxy image, final int rotationDegrees) {
        if (detectCamera.get()) {
            return;
        }
        startTime = System.currentTimeMillis();
        //用于控制识别时间间隔
        if((startTime-endTime)<time_interval){
            return;
        }
        detectCamera.set(true);
        final Bitmap bitmapsrc = imageToBitmap(image);  // 格式转换
        if (detectService == null) {
            detectCamera.set(false);
            return;
        }
        detectService.execute(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                width = bitmapsrc.getWidth();
                height = bitmapsrc.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, width, height, matrix, false);

                detectAndDraw(bitmap);
                showResultOnUI();
            }
        });
    }
    private Bitmap imageToBitmap(ImageProxy image) {
        byte[] nv21 = imagetToNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
    private byte[] imagetToNV21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ImageProxy.PlaneProxy y = planes[0];
        ImageProxy.PlaneProxy u = planes[1];
        ImageProxy.PlaneProxy v = planes[2];
        ByteBuffer yBuffer = y.getBuffer();
        ByteBuffer uBuffer = u.getBuffer();
        ByteBuffer vBuffer = v.getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] nv21 = new byte[ySize + uSize + vSize];
        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    //调用YOLOv5s模型
    protected void detectAndDraw(Bitmap image) {
        Box[] result = YOLOv5.detect(image, threshold, nms_threshold);
        detection_times++;
        if (result == null) {
            detectCamera.set(false);
            return;
        }
        mutableBitmap = drawBoxRects(image, result);
    }
    protected Bitmap drawBoxRects(Bitmap mutableBitmap, Box[] results) {
        if (results == null || results.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
        boxPaint.setTextSize(30 * mutableBitmap.getWidth() / 800.0f);
        NumOfPeople=0;
        for (Box box : results) {
            NumOfPeople++;
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel() + String.format(Locale.CHINESE, " %.3f", box.getScore()), box.x0 + 3, box.y0 + 30 * mutableBitmap.getWidth() / 1000.0f, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
        }
        //发起网络请求，更新数据库
        people_sum+=NumOfPeople;
        if(detection_times%request_frequency==0){
            request(people_sum/request_frequency);
        }
        people_sum=0;

        return mutableBitmap;
    }
    protected void showResultOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectCamera.set(false);
                resultImageView.setImageBitmap(mutableBitmap);

                endTime = System.currentTimeMillis();
                long dur = endTime - startTime;
                float fps = (float) (1000.0 / dur);
                total_fps = (total_fps == 0) ? fps : (total_fps + fps);
                fps_count++;

                tvInfo.setText(String.format(Locale.CHINESE,
                        "Size: %dx%d\nTime: %.3f s\nFPS: %.3f\nAVG_FPS: %.3f\nNumber of people: %d\nDetection times： %d\nid:"+location_id,
                        height, width, dur / 1000.0, fps, (float) total_fps / fps_count,NumOfPeople,detection_times));
            }
        });
    }

    @Override
    protected void onDestroy() {
        detectCamera.set(false);
        if (detectService != null) {
            detectService.shutdown();
            detectService = null;
        }
        if (mmr != null) {
            mmr.release();
        }
        CameraX.unbindAll();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission!", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    //发起网络请求
    public void request(int number) {
        System.out.println("开始请求");

        //对 发送请求 进行封装
        Call<Message> call = request.getCall(new NumberOfPeople(location_id,number));

        //步骤6:发送网络请求(异步)
        call.enqueue(new Callback<Message>() {
            //请求成功时回调
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                // 步骤7：处理返回的数据结果
                System.out.println(response.body().toString());
            }

            //请求失败时回调
            @Override
            public void onFailure(Call<Message> call, Throwable throwable) {
                System.out.println("请求失败");
                System.out.println(throwable.getMessage());
            }
        });
    }

}
