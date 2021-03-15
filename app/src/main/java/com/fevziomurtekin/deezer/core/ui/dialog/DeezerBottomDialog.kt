package com.fevziomurtekin.deezer.core.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.fevziomurtekin.deezer.R
import com.fevziomurtekin.deezer.databinding.DialogBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeezerBottomDialog<T> constructor(
    val style: BottomSheetType = BottomSheetType.ALBUM,
    bottomSheetData: T
) : BottomSheetDialogFragment() {

  private var binding: DialogBottomSheetBinding? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? = view?.let { safeView->
    binding = DataBindingUtil.bind(safeView)
    binding?.root
  }

}