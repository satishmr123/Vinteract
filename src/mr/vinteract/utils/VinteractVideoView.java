package mr.vinteract.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VinteractVideoView extends VideoView {

	private int mWidth;
	private int mHeight;
	
	public VinteractVideoView(Context context) {
		super(context);
	}
	public VinteractVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public VinteractVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setVideoAspect(int w,int h){
		mWidth=w;
		mHeight=h;
	    measure(w, h);
	    requestLayout();
	    invalidate();
	}
	 @Override
	 protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	 {
	     super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	     if(mWidth!=0 && mHeight!=0)
	     setMeasuredDimension(mWidth,mHeight);
	 }
}
