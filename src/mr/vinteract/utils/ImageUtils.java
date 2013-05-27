
package mr.vinteract.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

public class ImageUtils {
	
	/** Generate Bitmap from input file -> YUV420(NV21) -> Save to Jpeg(for testing) */
	public static void BitmaptoJpeg (String inputJpegfilename)
	{
		int Width, Height;
		String imageFilePath = get_FilePath (inputJpegfilename, ".jpg");
		//Read from test Jpeg image
		Log.d("", "---------- get bitmap from input jpeg file = " + imageFilePath);
		Bitmap bitmapimage = BitmapFactory.decodeFile(imageFilePath);
		
		if (bitmapimage == null)
		{
			Log.e("", "---------- Unable to open the file");
			return;
		}
		
		Width = bitmapimage.getWidth();
		Height = bitmapimage.getHeight();
		
		Log.d("", "---------- Width = " + Width + " Height = " + Height);
		Log.d("", "---------- Convert RGB to YUV--------");
		
		long startTime = System.currentTimeMillis();
		byte [] yuvBuffer = GetYUV(Width, Height, bitmapimage);
		long endTime = System.currentTimeMillis();
		
		Log.d("", "---------- TimeTaken for RGBtoYUVConversion of " + inputJpegfilename  + " file = " + (endTime - startTime) + " ms");
		
		bitmapimage.recycle();
		
		Log.d("", "---------- YUV is returned. Create JPEG out of it");
		String filePath = convert_jpeg(yuvBuffer, Width, Height, 100, ImageFormat.NV21, inputJpegfilename);
		
		Log.d("---------  Reconstructed Jpeg File saved at = ", filePath);
	}
	
		
	public static byte [] GetYUV(int inputWidth, int inputHeight, Bitmap bitmapimage) {

        int [] pixels = new int[inputWidth * inputHeight];

        // Memcopy RGB to an array
        //getPixels (int[] pixels, int offset, int stride, int x, int y, int width, int height)
        //Returns in pixels[] a copy of the data in the bitmap. Each value is a packed int representing a Color. 
        bitmapimage.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        long startTime = System.currentTimeMillis();
        colorconvert(yuv, pixels, inputWidth, inputHeight);
        long endTime = System.currentTimeMillis();
        Log.d("", "---------- TimeTaken for colorconvert of " + "-- file = " + (endTime - startTime) + " ms");
        return yuv;
    }

	/** Color convert to YUVNV21 format = plane of Y + interleaved planes of VU
	 * @param output yuv will be stored here
	 * @param input argb byte array
	 * @param width 
	 * @param height*/
	public static void colorconvert(byte[] yuv, int[] aRGB, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                //a = (aRGB[index] & 0xff000000) >> 24; 
                R = (aRGB[index] & 0xff0000) >> 16;
                G = (aRGB[index] & 0xff00) >> 8;
                B = (aRGB[index] & 0xff) >> 0;

                Y = ((66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = (( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = (( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                
                if (j % 2 == 0 && index % 2 == 0) 
                { 
                    yuv[uvIndex++] = (byte)((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv[uvIndex++] = (byte)((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }

	/** Convert input yuv to Jpeg file
	 * @param yuv byte array
	 * @param width
	 * @param height
	 * @param quality factor for jpeg
	 * @param image format ImageFormat.NV21 is used
	 * @param input jpeg file name*/
	public static String convert_jpeg (final byte[] byteBuffer, int ImageWidth, int ImageHeight, final int QualityFactor, int imageFormat, String inputJpegfilename) {
	    String imageFilePath = null;
	    YuvImage yuvImage = null;
	    
	    if ((imageFormat == ImageFormat.NV21) || (imageFormat == ImageFormat.YUY2))
	    {
	        Rect rect = new Rect(0, 0, ImageWidth, ImageHeight); 
	        try {
	        	yuvImage = new YuvImage(byteBuffer, imageFormat, ImageWidth, ImageHeight, null);
	        } catch (IllegalArgumentException iLLAE) {
	        	iLLAE.printStackTrace();
	            imageFilePath = null;
	            return null;
	        }
	        
	        OutputStream outStream = null;
	        imageFilePath = get_FilePath (inputJpegfilename + "_reconstructed", ".jpg");
	        File jPegFile = new File(imageFilePath);
	        try 
	        {
	            outStream = new FileOutputStream(jPegFile);
	            yuvImage.compressToJpeg(rect, QualityFactor, outStream);
	            outStream.flush();
	            outStream.close();
	        } 
	        catch (FileNotFoundException e) 
	        {
	            e.printStackTrace();
	            imageFilePath = null;
	        }
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	            imageFilePath = null;
	        }
	    }
	    return imageFilePath;
	}
	
	public static String get_FilePath (String filename, String ext)
	{
		String StorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/";
		final StringBuilder mediaFileBuilder = new StringBuilder(200);
		mediaFileBuilder.append(StorageDir).append(filename).append(ext);
		return mediaFileBuilder.toString();
	}
	
}