package de.hsfl.PixelPioneers.FlagFury

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hsfl.PixelPioneers.FlagFury.databinding.ListRowBinding
import org.json.JSONObject

class PlayerAdapter(private var playerList: List<JSONObject>) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = playerList[position]
        holder.bind(player)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(playerList: List<JSONObject>) {
       this.playerList = playerList
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: ListRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: JSONObject) {
            val name = player.getString("name")
            val team = player.getInt("team")
            val bluetoothStatus = player.optString("addr")

            binding.name.text = name
            binding.team.text = team.toString()
           if (!bluetoothStatus.isNullOrEmpty()) {
               binding.bluetoothStatus.setImageResource(android.R.drawable.btn_star_big_on)
            }


        }
    }
}
