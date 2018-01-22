package com.startek.fm220;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by ivan.lin on 2017/7/12.
 */
public class DialogUtil {
    public static Dialog waitingDialog(Context context, boolean canelable) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.waiting_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(canelable);

        return dialog;
    }

    public static void showToast(Context context, int resId) {
        Toast t = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0 ,0);
        t.show();
    }

    /*
    public static void showToast(Context context, String s) {
        Toast t = Toast.makeText(context, s, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0 ,0);
        t.show();
    }
    */

    public static void showToast(Context context, @StringRes int resId, Object... formatArgs) {
        Toast t;
        String s;

        if (resId == R.string.general_request_error) {
            int code = (int) formatArgs[0];

            if (code == 103) {
                s = context.getString(resId, 103, context.getString(R.string.token_expired));
            } else {
                s = context.getString(resId, formatArgs);
            }
        } else {
            s = context.getString(resId, formatArgs);
        }

        t = Toast.makeText(context, s, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0 ,0);
        t.show();
    }
}
