package com.app.personas_social.stickerview;

import android.view.MotionEvent;


/**
 * @author wupanjie
 */

public class DeleteIconEvent implements StickerIconEvent {


  public DeleteIconEvent() {


  }

  @Override
  public void onActionDown(StickerView stickerView, MotionEvent event) {

  }

  @Override
  public void onActionMove(StickerView stickerView, MotionEvent event) {

  }

  @Override
  public void onActionUp(StickerView stickerView, MotionEvent event) {
    if (stickerView.getOnStickerOperationListener() != null) {
      stickerView.getOnStickerOperationListener()
              .onStickerDeleted(stickerView.getCurrentSticker());
    }
    stickerView.removeCurrentSticker();
//    bsbTextColorFont.setState(); =


  }
}
