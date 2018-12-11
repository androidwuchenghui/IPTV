package com.iptv.hn.entity;

/**
 * Created by zhuxi on 2017/11/15.
 */
public class AppInfoBean {
    public JsonDataBean jsonData;

    public JsonDataBean getJsonData() {
        return jsonData;
    }

    public void setJsonData(JsonDataBean jsonData) {
        this.jsonData = jsonData;
    }

    public static class JsonDataBean{
       private String versionName;
       private int versionCode;
       private String packageName;

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getVersionName() {
             return versionName;
         }

         public void setVersionName(String versionName) {
             this.versionName = versionName;
         }

         public int getVersionCode() {
             return versionCode;
         }

         public void setVersionCode(int versionCode) {
             this.versionCode = versionCode;
         }
             }

}
