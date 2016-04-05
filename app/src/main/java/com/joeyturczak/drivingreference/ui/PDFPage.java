package com.joeyturczak.drivingreference.ui;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joeyturczak.drivingreference.R;

import java.io.File;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class PDFPage extends Fragment {

    int mPageNumber;
    String mFileName;

    private TextView mPageNumberView;
    private ImageView mPage;

    PhotoViewAttacher mAttacher;

    public PDFPage() {
        // Required empty public constructor
    }

    public static PDFPage newInstance() {
        return new PDFPage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAttacher != null) {
            mAttacher.cleanup();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);

        Bundle bundle = getArguments();
        if(bundle != null) {
            mPageNumber = bundle.getInt(getString(R.string.pdf_page_number_key));
            mFileName = bundle.getString(getString(R.string.pdf_file_name_key));
        }

        mPage = (ImageView) rootView.findViewById(R.id.current_page);
        mPageNumberView = (TextView) rootView.findViewById(R.id.page_number);

        PdfRenderer.Page currentPage;

        File file = new File(getContext().getFilesDir(), mFileName);

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            currentPage = renderer.openPage(mPageNumber);

            int totalPages = renderer.getPageCount();

            int imageWidth = 180/72*currentPage.getWidth();
            int imageHeight = 180/72*currentPage.getHeight();

            Bitmap currentPageBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_4444);

            if(mPageNumber < 0) {
                mPageNumber = 0;
            } else if (mPageNumber > totalPages) {
                mPageNumber = totalPages - 1;
            }

            currentPage.render(currentPageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            currentPageBitmap.setHasAlpha(true);
            currentPageBitmap.setHasMipMap(true);

            mPage.setImageBitmap(currentPageBitmap);
            mPage.invalidate();

            mAttacher = new PhotoViewAttacher(mPage);

            mPageNumberView.setText(getString(R.string.pdf_page_number_text, mPageNumber + 1, totalPages));

            currentPage.close();
            renderer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }
}
