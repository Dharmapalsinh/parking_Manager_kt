package com.dharmapal.parking_manager_kt.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dharmapal.parking_manager_kt.R
import com.dharmapal.parking_manager_kt.models.PriceModel

class PriceAdapter(private val context: Context, private val list: List<PriceModel>,val onclick:(PriceModel)->Unit) :
    RecyclerView.Adapter<PriceAdapter.ViewHolder>() {
    var lastPosition = 0
    var row_index = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bg: RelativeLayout
        var amount: TextView
        var type: TextView
        var check: ImageView

        init {
            bg = itemView.findViewById(R.id.bg)
            amount = itemView.findViewById<View>(R.id.amount) as TextView
            type = itemView.findViewById<View>(R.id.vehicleType) as TextView
            check = itemView.findViewById<View>(R.id.check) as ImageView
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.pricecard, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[holder.adapterPosition]
        holder.amount.text = model.amount
        holder.type.text = model.type
        holder.bg.setOnClickListener {
            row_index = position
            notifyDataSetChanged()
        }
        if (row_index == position) {
            holder.check.visibility = View.VISIBLE
        } else {
            holder.check.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onclick(model) }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}