package com.example.radzik.recipes.document

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Environment
import android.util.Log

import com.airnauts.toolkit.utils.BitmapUtils
import com.example.radzik.recipes.database.EditTextPref
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.utils.ImageEditor
import com.google.firebase.auth.FirebaseAuth
import com.itextpdf.text.BadElementException
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.List
import com.itextpdf.text.ListItem
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.FontSelector
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfImage
import com.itextpdf.text.pdf.PdfIndirectObject
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

/**
 * Created by Radzik on 08.09.2017.
 */

//  -------- -------- -------- -------- -------- HOW TO USE! -------- -------- -------- -------- -------- //

// 1. Prepare enviroment for PDF creator (it creates file, document etc.)
// 2. Use a method writeDocument() to get Recipe parts from list and transfer those to a given document
// 3.
// 4.

//  -------- -------- -------- -------- -------- HOW TO USE! -------- -------- -------- -------- -------- //


class DocumentCreator {
    private var mPdfFolder: File? = null
    var recipeFile: File? = null
        private set
    private val mOutput: OutputStream? = null
    private var mDocument: Document? = null
    private val mManager: RecipeManager
    private var mPdfWriter: PdfWriter? = null
    private var mPdfFullPath: String? = null

    // parts of a document
    private var mMainTable: PdfPTable? = null
    private val mIngredientsTable: PdfPTable? = null

    // used to check whether height of a cell was compared to height of a photo
    private val mIsFirstCell = true

    init {
        mManager = RecipeManager.instance
    }

    fun prepareEnviromentAndwriteDocument(recipe: Recipe, activity: Activity) {

        mPdfFolder = File(Environment.getExternalStorageDirectory(), "recipes")
        if (!mPdfFolder!!.exists()) {
            mPdfFolder!!.mkdir()
            Log.i("Directory", "Pdf directory created")
        }
        Log.e("d", "mpdfFolder" + mPdfFolder!!)

        try {
            mDocument = Document(PageSize.A4.rotate()) // new Document created
            val path = "/" + FirebaseAuth.getInstance().currentUser!!.uid + "-" + recipe.title + ".pdf"
            mPdfFullPath = Environment.getExternalStorageDirectory().toString() + "/recipes" + path
            mPdfWriter = PdfWriter.getInstance(mDocument!!, FileOutputStream(mPdfFullPath!!))
            doTheWriting(recipe, activity)
            Log.d("OK", "done")

            recipeFile = File(mPdfFullPath!!)
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: DocumentException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

    @Throws(FileNotFoundException::class, DocumentException::class)
    private fun doTheWriting(recipe: Recipe, activity: Activity) {

        val cellHeightMeasure = 0f

        val list = recipe.list

        mDocument!!.open()

        val fontSelector = FontSelector()
        val f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14f)
        f1.color = BaseColor.BLACK
        fontSelector.addFont(f1)

        // adds a header to pdf
        addRecipeHeader(recipe)

        // prepares main table
        prepareMainTable()

        // write ingredients to the ingredients table:


        // add photo to cell
        val photoCell: PdfPCell? = null

        mDocument!!.close()

    }

    fun resetInstance() {
        mInstance = null
    }

    private fun createBgChunk(s: String?, font: Font): Chunk {
        val chunk = Chunk(s, font)
        chunk.setBackground(BaseColor.LIGHT_GRAY)
        return chunk
    }

    private fun addRecipeHeader(recipe: Recipe) {
        val paragraphTitleHeader = Paragraph()
        paragraphTitleHeader.alignment = Element.ALIGN_CENTER
        val code = Font(Font.FontFamily.COURIER, 24f, Font.NORMAL, BaseColor.BLACK)
        paragraphTitleHeader.add(createBgChunk(recipe.title, code))
        try {
            mDocument!!.add(paragraphTitleHeader)
        } catch (e: DocumentException) {
            e.printStackTrace()
        }

    }

    private fun prepareMainTable() {

        // checks if photo is to be adjusted to right or left side of a document
        mMainTable = PdfPTable(3)

        mMainTable!!.totalWidth = mDocument!!.pageSize.width - 50
        mMainTable!!.isLockedWidth = true
        try {
            mMainTable!!.setWidths(floatArrayOf(1f, 2f))
        } catch (e: DocumentException) {
            e.printStackTrace()
        }

    }

    private fun setBackground() {
        val canvas = mPdfWriter!!.directContentUnder
        var image: Image? = null
        try {
            image = Image.getInstance(ClassLoader.getSystemResource("recipe_bckg_light_bronze.png"))
        } catch (e: BadElementException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        image!!.scaleAbsolute(PageSize.A4.rotate())
        image.setAbsolutePosition(0f, 0f)

        try {
            canvas.addImage(image)
        } catch (e: DocumentException) {
            e.printStackTrace()
        }

    }

    companion object {

        private var mInstance: DocumentCreator? = null

        val instance: DocumentCreator
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = DocumentCreator()
                }

                return mInstance
            }
    }
}
