package sample.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item.view.*
import sample.Hobby
import sample.R
import sample.showToast

class HobbiesAdapter(private val context: Context, private val hobbiesList: List<Hobby>) :
    RecyclerView.Adapter<HobbiesAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentHobby: Hobby? = null
        private var pos: Int = 0

        init {
            itemView.setOnClickListener {
                context.showToast(currentHobby!!.title)
            }

            itemView.imgShare.setOnClickListener {
                currentHobby?.let {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, "My Hobby is: " + currentHobby!!.title)
                    intent.type = "text/plain"
                    context.startActivity(Intent.createChooser(intent, "Share to: "))
                }
            }
        }

        fun setData(hobby: Hobby?, position: Int) {
            hobby?.let {
                itemView.txtTitle.text = hobby.title
                this.pos = position
                this.currentHobby = hobby
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return hobbiesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(hobbiesList[position], position)
    }


}