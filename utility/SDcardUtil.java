package com.iptv.hn.utility;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
//import android.provider.DocumentsContract;

public class SDcardUtil {

    public static final int MAX_UPLOAD_IMG_LIMIT = 5 * 1024 * 1024;
    public static final int MAX_CROP_IMG_LIMIT = 1 * 1024 * 1024;

    public static final String CONTENT_EXTENRNAL_IMAGE_STRING = "content://media/external/images/media/";

    public static final String COMPRESS_TMP_FILE = getImageCachePath() + "dish_compress.jpg";

    static String getImageCachePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(SDcardUtil.getRoot().getAbsolutePath());
        sb.append("/iptv/pics/");

        String path = sb.toString();
        FileUtil.createDirIfMissed(path);
        return path;
    }

    /**
     * 判断SD卡是否存�?     * 
     * @return
     */
    public static boolean sdcardExists() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录
     *
     * @return
     */
    public static File getRoot() {
        File file = Environment.getExternalStorageDirectory();
        return file;
    }

    /**
     * 获取SD卡剩余的容量大小
     *
     * @return
     */
    public static long getFreeSpace() {
        File root = getRoot();
        StatFs stat = new StatFs(root.getPath());
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks;
    }

    public static void compressBitmap(String imgFile) throws Exception{
        ExifInterface exifInterface = new ExifInterface(imgFile);
        File targetFile = new File(imgFile);
        Bitmap image = null;
        if (targetFile.length() > MAX_CROP_IMG_LIMIT) {
            image = ImageUtil.getLargeBitmap(imgFile, 4);
        } else {
            image = ImageUtil.getLargeBitmap(imgFile, 1);
        }

        File tmpFile = new File(COMPRESS_TMP_FILE);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        tmpFile.createNewFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 60, new FileOutputStream(tmpFile));
    }

    /**
     * 批量上传图片的时候， 如果都用默认名的话，最终上传的是同一张压缩后的图片
     * @param imgFile
     * @param fileName
     * @throws Exception
     */
    public static void compressBitmap(String imgFile, String fileName) throws Exception{
        ExifInterface exifInterface = new ExifInterface(imgFile);
        File targetFile = new File(imgFile);
        Bitmap image = null;
        if (targetFile.length() > MAX_CROP_IMG_LIMIT) {
            image = ImageUtil.getLargeBitmap(imgFile, 4);
        } else {
            image = ImageUtil.getLargeBitmap(imgFile, 1);
        }

        File tmpFile = new File(getCompressFile(fileName));
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        tmpFile.createNewFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 60, new FileOutputStream(tmpFile));
    }

    public static final String getCompressFile(String fileName) {
        return getImageCachePath() + fileName + "_compress.jpg";
    }

    /**
     * 从相册获取图片是获取图片的真实路�?不同手机有两种方�?     * 
     * @param cxt
     * @param uri
     * @return
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    public static String getAbsoluteFilePath(Context cxt, Uri uri) {
        String path = "unknown";
        String schema = uri.getScheme().toString();
        if (schema.startsWith("content")) {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(DocumentsContract.isDocumentUri(cxt, uri)) {
                    String wholeID = DocumentsContract.getDocumentId(uri);
                    String id = wholeID.split(":")[1];  //wholeID = "image:xxxx"
                    String[] column = { MediaStore.Images.Media.DATA };
                    cursor = cxt.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
                            MediaStore.Images.Media._ID + "=?", new String[] { id }, null);
                } else {
                    CursorLoader loader = new CursorLoader(cxt, uri, proj, null, null, null);
                    cursor = loader.loadInBackground();
                }
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                CursorLoader loader = new CursorLoader(cxt, uri, proj, null, null, null);
                cursor = loader.loadInBackground();
            } else {
                cursor = ((Activity) cxt).managedQuery(uri, proj, null, null, null);
            }
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(columnIndex);
        } else if (schema.startsWith("file")) {
            path = uri.getLastPathSegment().toString();
        } else {
            path = path + "_" + uri.getLastPathSegment();
        }

        return path;
    }


    /**
     *
     * 根据文件路径获取图片在数据库中的ID
     *
     * @param cxt
     * @param filePath
     * @return
     */
    public static long getImageIdFromFilePath(Context cxt, String filePath) {

        long id = 0;

        // This returns us content://media/external/videos/media (or something like that)
        // I pass in "external" because that's the MediaStore's name for the external
        // storage on my device (the other possibility is "internal")
        Uri imageUri = MediaStore.Images.Media.getContentUri("external");
        Cursor cursor = null;

        ContentResolver contentResolver = cxt.getContentResolver();
        String[] projection = { MediaStore.Images.Media._ID };

        // TODO This will break if we have no matching item in the MediaStore.

        cursor = contentResolver.query(imageUri, projection, MediaStore.Images.Media.DATA + " LIKE ?",
                new String[] { filePath }, null);
        if (cursor != null) {
            try {

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(projection[0]);
                id = cursor.getLong(columnIndex);
            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        return id;
    }

}
