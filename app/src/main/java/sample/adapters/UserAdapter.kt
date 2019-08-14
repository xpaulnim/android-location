package sample.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
import sample.R
import sample.model.User

class UserAdapter(private val context: Context, private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var user: User? = null
        private var pos: Int = 0

        fun setData(user: User?, pos: Int) {
            user?.let {
                itemView.txtTitle.text = user.firstName
                this.pos = pos
                this.user = user
            }
        }

    }

    override fun onCreateViewHolder(parentViewGroup: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parentViewGroup, false))
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setData(users[position], position)
    }

}