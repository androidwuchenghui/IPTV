package com.iptv.hn.entity;

/**
 * Created by zhuxi on 2017/11/14.
 */
public class StartAppsBean {


    /**
     * className :
     * jsonData : {}
     * packageName : com.vunke.apptvstore
     */

    private StartAppBean startApp;

    public StartAppBean getStartApp() {
        return startApp;
    }

    public void setStartApp(StartAppBean startApp) {
        this.startApp = startApp;
    }

    @Override
    public String toString() {
        return "StartAppsBean{" +
                "startApp=" + startApp +
                '}';
    }

    public static class StartAppBean {
        private String className;
        private JsonDataBean jsonData;
        private String packageName;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public JsonDataBean getJsonData() {
            return jsonData;
        }

        public void setJsonData(JsonDataBean jsonData) {
            this.jsonData = jsonData;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public static class JsonDataBean {
            private String appId;
            private String outDown;

            public String getAppId() {
                return appId;
            }

            public void setAppId(String appId) {
                this.appId = appId;
            }

            public String getOutDown() {
                return outDown;
            }

            public void setOutDown(String outDown) {
                this.outDown = outDown;
            }

            @Override
            public String toString() {
                return "JsonDataBean{" +
                        "appId='" + appId + '\'' +
                        ", outDown='" + outDown + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "StartAppBean{" +
                    "className='" + className + '\'' +
                    ", jsonData=" + jsonData +
                    ", packageName='" + packageName + '\'' +
                    '}';
        }
    }
}
