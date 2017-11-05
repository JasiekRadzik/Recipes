package com.example.radzik.recipes.document;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

import com.airnauts.toolkit.utils.BitmapUtils;
import com.example.radzik.recipes.database.EditTextPref;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.utils.ImageEditor;
import com.google.firebase.auth.FirebaseAuth;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Radzik on 08.09.2017.
 */

//  -------- -------- -------- -------- -------- HOW TO USE! -------- -------- -------- -------- -------- //

// 1. Prepare enviroment for PDF creator (it creates file, document etc.)
// 2. Use a method writeDocument() to get Recipe parts from list and transfer those to a given document
// 3.
// 4.

//  -------- -------- -------- -------- -------- HOW TO USE! -------- -------- -------- -------- -------- //


public class DocumentCreator {

    private static DocumentCreator mInstance = null;
    private File mPdfFolder;
    private File mMyRecipeFile = null;
    private OutputStream mOutput;
    private Document mDocument;
    private RecipeManager mManager;
    private PdfWriter mPdfWriter;
    private String mPdfFullPath = null;

    // parts of a document
    private PdfPTable mainTable;

    // used to check whether height of a cell was compared to height of a photo
    private boolean mIsFirstCell = true;

    public static synchronized DocumentCreator getInstance() {
        if (mInstance == null) {
            mInstance = new DocumentCreator();
        }

        return mInstance;
    }

    public DocumentCreator() {
        mManager = RecipeManager.getInstance();
    }

    public void prepareEnviromentAndwriteDocument(Recipe recipe, Activity activity) {

        mPdfFolder = new File(Environment.getExternalStorageDirectory(), "recipes");
        if (!mPdfFolder.exists()) {
            mPdfFolder.mkdir();
            Log.i("Directory", "Pdf directory created");
        }
        Log.e("d", "mpdfFolder" + mPdfFolder);

        try {
            mDocument = new Document(PageSize.A4.rotate()); // new Document created
            String path = "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "-" + recipe.getTitle() + ".pdf";
            mPdfFullPath = Environment.getExternalStorageDirectory() + "/recipes" + path;
            mPdfWriter = PdfWriter.getInstance(mDocument, new FileOutputStream(mPdfFullPath));
            doTheWriting(recipe, activity);
            Log.d("OK", "done");

            mMyRecipeFile = new File(mPdfFullPath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void doTheWriting(Recipe recipe, Activity activity) throws FileNotFoundException, DocumentException {

        float cellHeightMeasure = 0;

        ArrayList<EditTextPref> list = recipe.getList();

        mDocument.open();

        FontSelector fontSelector = new FontSelector();
        Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14);
        f1.setColor(BaseColor.BLACK);
        fontSelector.addFont(f1);

        // adds a header to pdf
        addRecipeHeader(recipe);

        // prepares a table
        PdfPTable table = prepareTable(recipe);

        // add photo to cell
        PdfPCell photoCell = null;

        Image image = null;
        try {
            image = Image.getInstance(String.format("/storage/emulated/0/Pictures/cheeseCake.jpeg", "Photo"));
            photoCell = new PdfPCell(image, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDocument.close();

    }

    public void resetInstance() {
        mInstance = null;
    }

    public File getRecipeFile() {
        return mMyRecipeFile;
    }

    private Chunk createBgChunk(String s, Font font) {
        Chunk chunk = new Chunk(s, font);
        chunk.setBackground(BaseColor.LIGHT_GRAY);
        return chunk;
    }

    private void addRecipeHeader(Recipe recipe) {
        Paragraph paragraphTitleHeader = new Paragraph();
        paragraphTitleHeader.setAlignment(Element.ALIGN_CENTER);
        Font code = new Font(Font.FontFamily.COURIER, 24, Font.NORMAL, BaseColor.BLACK);
        paragraphTitleHeader.add(createBgChunk(recipe.getTitle(), code));
        try {
            mDocument.add(paragraphTitleHeader);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private PdfPTable prepareTable(Recipe recipe) {

        // checks if photo is to be adjusted to right or left side of a document
        PdfPTable mainTable = new PdfPTable(2);

        mainTable.setTotalWidth(mDocument.getPageSize().getWidth() - 50);
        mainTable.setLockedWidth(true);
        try {
            mainTable.setWidths(new float[]{1, 2});
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        return mainTable;
    }

    private void setBackground() {
        PdfContentByte canvas = mPdfWriter.getDirectContentUnder();
        Image image = null;
        try {
            image = Image.getInstance(ClassLoader.getSystemResource("recipe_bckg_light_bronze.png"));
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        image.scaleAbsolute(PageSize.A4.rotate());
        image.setAbsolutePosition(0, 0);

        try {
            canvas.addImage(image);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
