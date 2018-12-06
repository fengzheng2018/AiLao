package com.android.ailao.picture;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 为RecyclerView传入参数
 */
public class PictureItem {
    private Bitmap bitmap;
    private ImageView cancelImg;
    private String picName;

    public PictureItem(Bitmap bitmap, ImageView cancelImg, String picName) {
        this.bitmap = bitmap;
        this.cancelImg = cancelImg;
        this.picName = picName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ImageView getCancelImg() {
        return cancelImg;
    }

    public String getPicName() {
        return picName;
    }
}
