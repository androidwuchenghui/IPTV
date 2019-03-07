package com.iptv.hn.entity;

/**
 * Created by Administrator on 2017/2/20.
 */

public class AdsBean{

    //"special_type":1,"show_time":6,"file_type":1,"msg_id":1,"special_url":"http://127.0.0.1","priority_level":2
    private long msg_id;
    private int show_time;
    private int model_id = 2; //模板主题编号

    private int file_type; //文件类型，1为图片，2为gif
    private String file_url;

    private  String jsonData;

    public String video_url;

    public String time_text;

    public String time_color;

    public String pay_type;

    private int special_type; //跳转类型，1为url,2为app
    private String special_url; //跳转地址
    private int down_type = 1; //下载类型，1商店下载,2服务器下载
    private String down_url; //下载地址
    private int priority_level; //优先级,越小优先级越高
    private String float_position;
    private int busi_id;
    private long exce_starttime; //特定时间执行
    private long exce_endtime; //指定时间之前执行，过去丢弃

    public static final int ACTION_WEBVIEW = 1, ACTION_APP = 2,START_AMNGUO = 3;
    public static final int FILE_IMAGE = 1, FILE_GIF = 2,FILE_VOIDE =3,FILE_WEB=4,FILE_VOIDE_IJK =5,FILE_FLOAT_GIF=6;
    public static final int DOWNLOAD_FROM_STORE = 1, DOWNLOAD_FROM_SERVER = 2;

    private int position = SHOW_AT_RIGHT_BOTTOM;
    public static final int SHOW_AT_LEFT_TOP = 1, SHOW_AT_RIGHT_TOP = 2, SHOW_AT_LEFT_BOTTOM = 3, SHOW_AT_RIGHT_BOTTOM = 4, SHOW_AT_CENTER = 5,SHOW_AT_BT_CEN=6,SHOW_AT_lF_CEN=7,SHOW_AT_RI_CEN=8,SHOW_AT_TP_CEN=9;

    private int live_swift;

    private int is_back;

    public int getLive_swift() {
        return live_swift;
    }

    public void setLive_swift(int live_swift) {
        this.live_swift = live_swift;
    }

    public int getIs_back() {
        return is_back;
    }

    public void setIs_back(int is_back) {
        this.is_back = is_back;
    }

    public String getFloat_position() {
        return float_position;
    }

    public void setFloat_position(String float_position) {
        this.float_position = float_position;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public int getSpecial_type() {
        return special_type;
    }

    public void setSpecial_type(int special_type) {
        this.special_type = special_type;
    }

    public int getShow_time() {
        return show_time;
    }
    public int getShow_time(int i) {
        return show_time-i;
    }

    public void setShow_time(int show_time) {
        this.show_time = show_time;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public int getFile_type() {
        return file_type;
    }

    public void setFile_type(int file_type) {
        this.file_type = file_type;
    }

    public long getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(long msg_id) {
        this.msg_id = msg_id;
    }

    public String getSpecial_url() {
        return special_url;
    }

    public void setSpecial_url(String special_url) {
        this.special_url = special_url;
    }

    public int getPriority_level() {
        return priority_level;
    }

    public void setPriority_level(int priority_level) {
        this.priority_level = priority_level;
    }

    public int getModel_id() {
        return model_id;
    }

    public void setModel_id(int model_id) {
        this.model_id = model_id;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public int getDown_type() {
        return down_type;
    }

    public void setDown_type(int down_type) {
        this.down_type = down_type;
    }

    public String getDown_url() {
        return down_url;
    }

    public void setDown_url(String down_url) {
        this.down_url = down_url;
    }

    public long getExce_starttime() {
        return exce_starttime;
    }

    public void setExce_starttime(long exce_starttime) {
        this.exce_starttime = exce_starttime;
    }

    public long getExce_endtime() {
        return exce_endtime;
    }

    public void setExce_endtime(long exce_endtime) {
        this.exce_endtime = exce_endtime;
    }

    public int getBusi_id() {
        return busi_id;
    }

    public void setBusi_id(int busi_id) {
        this.busi_id = busi_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getTime_text() {
        return time_text;
    }

    public void setTime_text(String time_text) {
        this.time_text = time_text;
    }

    public String getTime_color() {
        return time_color;
    }

    public void setTime_color(String time_color) {
        this.time_color = time_color;
    }

    @Override
    public String toString() {
        return "AdsBean{" +
                "msg_id=" + msg_id +
                ", show_time=" + show_time +
                ", model_id=" + model_id +
                ", file_type=" + file_type +
                ", file_url='" + file_url + '\'' +
                ", jsonData='" + jsonData + '\'' +
                ", video_url='" + video_url + '\'' +
                ", time_text='" + time_text + '\'' +
                ", time_color='" + time_color + '\'' +
                ", special_type=" + special_type +
                ", special_url='" + special_url + '\'' +
                ", down_type=" + down_type +
                ", down_url='" + down_url + '\'' +
                ", priority_level=" + priority_level +
                ", busi_id=" + busi_id +
                ", exce_starttime=" + exce_starttime +
                ", exce_endtime=" + exce_endtime +
                ", position=" + position +
                '}';
    }
}
