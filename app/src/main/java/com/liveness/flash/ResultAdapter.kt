package com.liveness.flash

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Created by Hieudt43 on 20/10/2024.
 */
class ResultAdapter(private val map: HashMap<Long, String>): RecyclerView.Adapter<ResultAdapter.ResultHolder>() {
    private var entries : List<Map.Entry<Long, String>>? = null

    init {
        entries = map.entries.toList()
    }
    inner class ResultHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView= itemView.findViewById(R.id.tvTitle)
        val ivResult: ImageView= itemView.findViewById(R.id.ivResult)

         fun bindView(title: Long, image: String){
            tvTitle.text = title.toString()
            val img = base64ToBitmap(image)
            if (img != null) {
                ivResult.setImageBitmap(img)
            }
        }
    }

    fun base64ToBitmap(b64Data: String?): Bitmap? {
        return try {
            val decodedString = Base64.decode(b64Data, Base64.DEFAULT)
            val inputStream: InputStream = ByteArrayInputStream(decodedString)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Error) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
        return ResultHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false))
    }

    override fun getItemCount(): Int {
        return map.size
    }

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        val (key, value) = entries?.get(position)!!
        holder.bindView(key, value)
    }
}