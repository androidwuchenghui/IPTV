package com.iptv.hn.utility;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RelativeLayout;

import com.iptv.hn.entity.float_position;

/* 
 * 获取、设置控件信息 
 */  
public class WidgetController {  
  
    /* 
     * 获取控件宽 
     */  
    public static int getWidth(View view)  
    {  
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        view.measure(w, h);  
        return (view.getMeasuredWidth());         
    }  
    /* 
     * 获取控件高 
     */  
    public static int getHeight(View view)  
    {  
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
        view.measure(w, h);  
        return (view.getMeasuredHeight());         
    }  
      
    /* 
     * 设置控件所在的位置X，并且不改变宽高， 
     * X为绝对位置，此时Y可能归0 
     */  
    public static void setLayoutX(View view,int x)  
    {  
        MarginLayoutParams margin=new MarginLayoutParams(view.getLayoutParams());  
        margin.setMargins(x,margin.topMargin, x+margin.width, margin.bottomMargin);  
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);  
        view.setLayoutParams(layoutParams);  
    }  
    /* 
     * 设置控件所在的位置Y，并且不改变宽高， 
     * Y为绝对位置，此时X可能归0 
     */  
    public static void setLayoutY(View view,int y)  
    {  
        MarginLayoutParams margin=new MarginLayoutParams(view.getLayoutParams());  
        margin.setMargins(margin.leftMargin,y, margin.rightMargin, y+margin.height);  
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);  
        view.setLayoutParams(layoutParams);  
    }  
    /* 
     * 设置控件所在的位置YY，并且不改变宽高， 
     * XY为绝对位置 
     */  
    public static void setLayout(View view,int x,int y)
    {  
        MarginLayoutParams margin=new MarginLayoutParams(view.getLayoutParams());  
        margin.setMargins(x,y, x+margin.width, y+margin.height);  
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);  
    }
    public static void setLayoutViedo(View view, int x, int y, float_position float_position)
    {
        MarginLayoutParams margin=new MarginLayoutParams(view.getLayoutParams());
        margin.setMargins(x,y, x+margin.width, y+margin.height);
//        view.
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
//        layoutParams.topMargin = x+margin.width;
//        layoutParams.bottomMargin = y+margin.height;
        layoutParams.width =float_position.getFloat_width();
        layoutParams.height =float_position.getFloat_height();
//        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }
}  
