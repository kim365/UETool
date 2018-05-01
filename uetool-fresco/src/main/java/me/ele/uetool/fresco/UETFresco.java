package me.ele.uetool.fresco;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.drawable.FadeDrawable;
import com.facebook.drawee.drawable.ScaleTypeDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.GenericDraweeView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import me.ele.uetool.base.BitmapItem;
import me.ele.uetool.base.IAttrs;
import me.ele.uetool.base.Item;
import me.ele.uetool.base.TextItem;
import me.ele.uetool.base.TitleItem;

import static me.ele.uetool.base.Util.getDrawableBitmap;
import static me.ele.uetool.base.Util.px2dip;

public class UETFresco implements IAttrs {

  @Override public List<Item> getAttrs(View view) {
    List<Item> items = new ArrayList<>();
    if (view instanceof DraweeView) {
      items.add(new TitleItem("DraweeView"));
      items.add(new TextItem("CornerRadius", getCornerRadius((DraweeView) view)));
      items.add(new TextItem("ImageURI", getImageURI((DraweeView) view), true));
      items.add(new TextItem("ActualScaleType", getScaleType((DraweeView) view), true));
      items.add(new TextItem("IsSupportAnimation", isSupportAnimation((DraweeView) view)));
      items.add(new BitmapItem("PlaceHolderImage", getPlaceHolderBitmap((DraweeView) view)));
    }
    return items;
  }

  private String getCornerRadius(DraweeView draweeView) {
    GenericDraweeHierarchy hierarchy = getGenericDraweeHierarchy(draweeView);
    if (hierarchy != null) {
      RoundingParams params = hierarchy.getRoundingParams();
      if (params != null) {
        float[] cornersRadii = params.getCornersRadii();
        float firstRadii = cornersRadii[0];
        for (int i = 1; i < 8; i++) {
          if (firstRadii != cornersRadii[i]) {
            return null;
          }
        }
        return px2dip(draweeView.getContext(), firstRadii) + "dp";
      }
    }
    return null;
  }

  private String getScaleType(DraweeView draweeView) {
    GenericDraweeHierarchy hierarchy = getGenericDraweeHierarchy(draweeView);
    if (hierarchy != null) {
      return hierarchy.getActualImageScaleType().toString().toUpperCase();
    }
    return null;
  }

  private String getImageURI(DraweeView draweeView) {
    PipelineDraweeControllerBuilder builder = getFrescoControllerBuilder(draweeView);
    if (builder != null) {
      return builder.getImageRequest().getSourceUri().toString();
    }
    return "";
  }

  private String isSupportAnimation(DraweeView draweeView) {
    PipelineDraweeControllerBuilder builder = getFrescoControllerBuilder(draweeView);
    if (builder != null) {
      return String.valueOf(builder.getAutoPlayAnimations()).toUpperCase();
    }
    return "";
  }

  private Bitmap getPlaceHolderBitmap(DraweeView draweeView) {
    GenericDraweeHierarchy hierarchy = getGenericDraweeHierarchy(draweeView);
    if (hierarchy != null && hierarchy.hasPlaceholderImage()) {
      try {
        Field mFadeDrawableField = hierarchy.getClass().getDeclaredField("mFadeDrawable");
        mFadeDrawableField.setAccessible(true);
        FadeDrawable fadeDrawable = (FadeDrawable) mFadeDrawableField.get(hierarchy);
        Field mLayersField = fadeDrawable.getClass().getDeclaredField("mLayers");
        mLayersField.setAccessible(true);
        Drawable[] layers = (Drawable[]) mLayersField.get(fadeDrawable);
        // PLACEHOLDER_IMAGE_INDEX == 1
        Drawable drawable = layers[1];
        return getFrescoDrawableBitmap(drawable);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private GenericDraweeHierarchy getGenericDraweeHierarchy(DraweeView draweeView) {
    if (draweeView instanceof GenericDraweeView) {
      return ((GenericDraweeView) draweeView).getHierarchy();
    }
    return null;
  }

  private PipelineDraweeControllerBuilder getFrescoControllerBuilder(DraweeView draweeView) {
    try {
      PipelineDraweeController controller = (PipelineDraweeController) draweeView.getController();
      Field mDataSourceSupplierFiled =
          PipelineDraweeController.class.getDeclaredField("mDataSourceSupplier");
      mDataSourceSupplierFiled.setAccessible(true);
      Supplier supplier = (Supplier) mDataSourceSupplierFiled.get(controller);
      Field mAutoField =
          Class.forName("com.facebook.drawee.controller.AbstractDraweeControllerBuilder$2")
              .getDeclaredField("this$0");
      mAutoField.setAccessible(true);
      PipelineDraweeControllerBuilder builder =
          (PipelineDraweeControllerBuilder) mAutoField.get(supplier);
      return builder;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private Bitmap getFrescoDrawableBitmap(Drawable drawable) {
    try {
      if (drawable instanceof ScaleTypeDrawable) {
        return ((BitmapDrawable) drawable.getCurrent()).getBitmap();
      }
      return getDrawableBitmap(drawable);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}