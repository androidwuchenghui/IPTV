package com.iptv.hn.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtil {

    public static Bitmap getLargeBitmap(String sdcardPath, int inSampleSize) throws FileNotFoundException {

        InputStream is = new FileInputStream(sdcardPath);
        //2.为位图设置100K的缓存
        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inTempStorage = new byte[100 * 1024];
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        //4.设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
        //		opts.inPurgeable = true;
        //5.设置位图缩放比例
        //width，hight设为原来的四分一（该参数请使用2的整数倍）,这也减小了位图占用的内存大小；
        //例如，一张分辨率为2048*1536px的图像使用inSampleSize值为4的设置来解码，产生的Bitmap大小约为
        //512*384px。相较于完整图片占用12M的内存，这种方式只需0.75M内存(假设Bitmap配置为//ARGB_8888)。
        //		opts.inSampleSize = 4;
        opts.inSampleSize = inSampleSize;

        //6.设置解码位图的尺寸信息
        //		opts.inInputShareable = true;
        //7.解码位图
        Bitmap btp =BitmapFactory.decodeStream(is,null, opts);


        int degree = readPictureDegree(sdcardPath);
        if (degree != 0) {
            return toturn(btp, degree);
        }

        return btp;
    }

    static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    static Bitmap toturn(Bitmap img, int degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree); /*翻转90度*/
        int width = img.getWidth();
        int height =img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        return img;
    }

    /**
     * 屏幕宽
     *
     * @param context
     * @return
     */
    public static int getWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 屏幕高
     *
     * @param context
     * @return
     */
    public static int getHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 解决小米、魅族等定制ROM
     * @param context
     * @param intent
     * @return
     */
    public static Uri getUri(Context context , Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                        Log.i("urishi", uri.toString());
                    }
                }
            }
        }
        return uri;
    }

    /**
     * 根据文件Uri获取路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathByFileUri(Context context, Uri uri) {
        String filePath = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
        }
        cursor.close();
        return filePath;
    }

    /**
     * 根据图片原始路径获取图片缩略图
     *
     * @param imagePath 图片原始路径
     * @param width		缩略图宽度
     * @param height	缩略图高度
     * @return
     */
    public static Bitmap getImageThumbnail(String imagePath, int width,
                                           int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载直接获取Bitmap宽高
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        if(bitmap == null){
            // 计算缩放比
            int h = options.outHeight;
            int w = options.outWidth;
            Log.i("test", "optionsH"+h+"optionsW"+w);
            int beWidth = w / width;
            int beHeight = h / height;
            int rate = 1;
            if (beWidth < beHeight) {
                rate = beWidth;
            } else {
                rate = beHeight;
            }
            if (rate <= 0) {//图片实际大小小于缩略图,不缩放
                rate = 1;
            }
            options.inSampleSize = rate;
            options.inJustDecodeBounds = false;
            // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }


    public static boolean saveImageToSdcard(Bitmap bitmap, Context ctx) {

        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            String timestamp = "/" + formatter.format(new Date()) + ".png";

            File imageFile = new File(imageFilePath, timestamp);

            FileOutputStream ostream = null;
            try {
                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                }
                ostream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, ostream);
                ostream.flush();
                ostream.close();

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(imageFile);
                intent.setData(uri);
                ctx.sendBroadcast(intent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return false;
    }
    //保存到系统相册
    public static boolean saveImageToCamera(Bitmap bitmap, Context ctx) {

        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            File imageFilePath = Environment.getExternalStorageDirectory();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            String timestamp = "/" + formatter.format(new Date()) + ".png";

            File imageFile = new File(imageFilePath.getPath()+"/DCIM/Camera", timestamp);

            FileOutputStream ostream = null;
            try {
                if (!imageFile.exists()) {
                    imageFile.createNewFile();
                }
                ostream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, ostream);
                ostream.flush();
                ostream.close();

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(imageFile);
                intent.setData(uri);
                ctx.sendBroadcast(intent);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return false;
    }

}
