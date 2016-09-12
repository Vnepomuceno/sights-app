package pt.sights.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 *
 * @author 	Valter Nepomuceno
 * @version	1.0
 * @since	17th of December of 2014
 */
public class Graphics {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Bitmap addBlur(Bitmap source, Context context) {
		if (source != null) {
			Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
			RenderScript renderScript = RenderScript.create(context);
			ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

			Allocation allIn = Allocation.createFromBitmap(renderScript, source);
			Allocation allOut = Allocation.createFromBitmap(renderScript, output);

			blurScript.setRadius(10.f);

			blurScript.setInput(allIn);
			blurScript.forEach(allOut);

			allOut.copyTo(output);

			source.recycle();
			renderScript.destroy();

			return output;
		} else {
			return null;
		}
	}

	public static Bitmap applyDarkGradient(Bitmap source, int opacity) {
		if (source != null) {
			int gradient_height = 250;
			int width = source.getWidth();
			int height = source.getHeight();
			Bitmap overlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(overlay);

			canvas.drawBitmap(source, 0, 0, null);

			Paint paint = new Paint();
			Shader shader = new LinearGradient(0, height - gradient_height, 0, height,
					0x00000000, getHexadecimalBlack(opacity), Shader.TileMode.REPEAT);
			paint.setShader(shader);
			canvas.drawRect(0, height - gradient_height, width, height, paint);

			return overlay;
		} else {
			return null;
		}
	}

	private static int getHexadecimalBlack(int opacity) {
		switch (opacity) {
			case 100 : return 0xFF000000;
			case 95  : return 0xF2000000;
			case 90  : return 0xE6000000;
			case 85  : return 0xD9000000;
			case 80  : return 0xCC000000;
			case 75  : return 0xBF000000;
			case 70  : return 0xB3000000;
			case 65  : return 0xA6000000;
			case 60  : return 0x99000000;
			case 55  : return 0x8C000000;
			case 50  : return 0x80000000;
			case 45  : return 0x73000000;
			case 40  : return 0x66000000;
			case 35  : return 0x59000000;
			case 30  : return 0x4D000000;
			case 25  : return 0x40000000;
			case 20  : return 0x33000000;
			case 15  : return 0x26000000;
			case 10  : return 0x1A000000;
			case 5   : return 0x0D000000;
			case 0   : return 0x00000000;
		}

		return -1;
	}

}
