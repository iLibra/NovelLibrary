package io.github.gmathi.novellibrary.activity

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import io.github.gmathi.novellibrary.R
import io.github.gmathi.novellibrary.adapter.GenericAdapter
import io.github.gmathi.novellibrary.dataCenter
import io.github.gmathi.novellibrary.dbHelper
import io.github.gmathi.novellibrary.util.applyFont
import io.github.gmathi.novellibrary.util.setDefaults
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_recycler_view.*
import kotlinx.android.synthetic.main.listitem_title_subtitle_widget.view.*
import java.io.File

class ReaderSettingsActivity : BaseActivity(), GenericAdapter.Listener<String> {

    lateinit var adapter: GenericAdapter<String>
    private lateinit var settingsItems: ArrayList<String>
    private lateinit var settingsItemsDescription: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setRecyclerView()
    }

    private fun setRecyclerView() {
        settingsItems = ArrayList(resources.getStringArray(R.array.reader_titles_list).asList())
        settingsItemsDescription = ArrayList(resources.getStringArray(R.array.reader_subtitles_list).asList())
        adapter = GenericAdapter(items = settingsItems, layoutResId = R.layout.listitem_title_subtitle_widget, listener = this)
        recyclerView.setDefaults(adapter)
        recyclerView.addItemDecoration(object : DividerItemDecoration(this, VERTICAL) {

            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                val position = parent?.getChildAdapterPosition(view)
                if (position == parent?.adapter?.itemCount?.minus(1)) {
                    outRect?.setEmpty()
                } else {
                    super.getItemOffsets(outRect, view, parent, state)
                }
            }
        })
        swipeRefreshLayout.isEnabled = false
    }

    override fun bind(item: String, itemView: View, position: Int) {
        itemView.widgetChevron.visibility = View.INVISIBLE
        itemView.widgetSwitch.visibility = View.INVISIBLE
        itemView.widgetButton.visibility = View.INVISIBLE

        itemView.title.applyFont(assets).text = item
        itemView.subtitle.applyFont(assets).text = settingsItemsDescription[position]
        itemView.widgetSwitch.setOnCheckedChangeListener(null)
        when (position) {
            0 -> {
                itemView.widgetSwitch.visibility = View.VISIBLE
                itemView.widgetSwitch.isChecked = dataCenter.cleanChapters
                itemView.widgetSwitch.setOnCheckedChangeListener { _, value ->
                    dataCenter.cleanChapters = value
                    if (value) {
                        dataCenter.javascriptDisabled = value
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            1 -> {
                itemView.widgetSwitch.visibility = View.VISIBLE
                itemView.widgetSwitch.isChecked = dataCenter.javascriptDisabled
                itemView.widgetSwitch.setOnCheckedChangeListener { _, value ->
                    dataCenter.javascriptDisabled = value
                    if (!value) {
                        dataCenter.cleanChapters = value
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            2 -> {
                itemView.widgetSwitch.visibility = View.VISIBLE
                itemView.widgetSwitch.isChecked = dataCenter.japSwipe
                itemView.widgetSwitch.setOnCheckedChangeListener { _, value -> dataCenter.japSwipe = value }
            }
            3 -> {
                itemView.widgetSwitch.visibility = View.VISIBLE
                itemView.widgetSwitch.isChecked = dataCenter.showReaderScroll
                itemView.widgetSwitch.setOnCheckedChangeListener { _, value -> dataCenter.showReaderScroll = value }
            }
            4 -> {
                itemView.widgetSwitch.visibility = View.VISIBLE
                itemView.widgetSwitch.isChecked = dataCenter.showChapterComments
                itemView.widgetSwitch.setOnCheckedChangeListener { _, value -> dataCenter.showChapterComments = value }
            }

        }

        itemView.setBackgroundColor(if (position % 2 == 0) ContextCompat.getColor(this, R.color.black_transparent)
        else ContextCompat.getColor(this, android.R.color.transparent))
    }

    override fun onItemClick(item: String) {
//        if (item == getString(R.string.sync_interval)) {
//            showSyncIntervalDialog()
//        }
    }

    //region OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
    //endregion

    //region Delete Files
    private fun deleteFilesDialog() {
        MaterialDialog.Builder(this)
            .title(getString(R.string.clear_data))
            .content(getString(R.string.clear_data_description))
            .positiveText(R.string.clear)
            .negativeText(R.string.cancel)
            .onPositive { dialog, _ ->
                val progressDialog = MaterialDialog.Builder(this)
                    .title(getString(R.string.clearing_data))
                    .content(getString(R.string.please_wait))
                    .progress(true, 0)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .show()
                deleteFiles(progressDialog)
                dialog.dismiss()
            }
            .onNegative { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun deleteFiles(dialog: MaterialDialog) {
        try {
            deleteDir(cacheDir)
            deleteDir(filesDir)
            dbHelper.removeAll()
            dataCenter.saveSearchHistory(ArrayList())
            dialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                deleteDir(File(dir, children[i]))
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        } else {
            return false
        }
    }
    //endregion


}