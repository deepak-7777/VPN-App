package com.securevpn.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securevpn.app.R
import com.securevpn.app.data.model.ServerItem

/**
 * RecyclerView adapter for the server list.
 * Supports three view types: FASTEST header card, SECTION header, and SERVER item.
 */
class ServerAdapter(
    private val onServerClick: (ServerItem) -> Unit,
    private val onFastestClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_FASTEST = 0
        private const val VIEW_TYPE_HEADER  = 1
        private const val VIEW_TYPE_SERVER  = 2
    }

    /** Sealed class representing list items */
    sealed class ListItem {
        object FastestServer : ListItem()
        data class Header(val title: String) : ListItem()
        data class Server(val server: ServerItem) : ListItem()
    }

    private var items: List<ListItem> = emptyList()
    private var selectedServerId: String? = null

    /** Build the flat list from a raw server list */
    fun submitServers(servers: List<ServerItem>, selectedId: String? = null) {
        selectedServerId = selectedId
        val list = mutableListOf<ListItem>()

        // Fastest auto option
        list.add(ListItem.FastestServer)

        // Recommended section
        val recommended = servers.filter { it.isRecommended }
        if (recommended.isNotEmpty()) {
            list.add(ListItem.Header("RECOMMENDED"))
            recommended.forEach { list.add(ListItem.Server(it)) }
        }

        // All locations section
        list.add(ListItem.Header("ALL LOCATIONS"))
        servers.forEach { list.add(ListItem.Server(it)) }

        items = list
        notifyDataSetChanged()
    }

    fun updateSelectedServer(serverId: String?) {
        selectedServerId = serverId
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.FastestServer -> VIEW_TYPE_FASTEST
        is ListItem.Header        -> VIEW_TYPE_HEADER
        is ListItem.Server        -> VIEW_TYPE_SERVER
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FASTEST -> FastestViewHolder(
                inflater.inflate(R.layout.item_fastest_server, parent, false)
            )
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_server_header, parent, false)
            )
            else -> ServerViewHolder(
                inflater.inflate(R.layout.item_server, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.FastestServer -> (holder as FastestViewHolder).bind()
            is ListItem.Header        -> (holder as HeaderViewHolder).bind(item.title)
            is ListItem.Server        -> (holder as ServerViewHolder).bind(item.server)
        }
    }

    // ─── Fastest ViewHolder ───

    inner class FastestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: CardView = view.findViewById(R.id.card_fastest)
        fun bind() {
            card.setOnClickListener { onFastestClick() }
        }
    }

    // ─── Header ViewHolder ───

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tv: TextView = view as TextView
        fun bind(title: String) { tv.text = title }
    }

    // ─── Server ViewHolder ───

    inner class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card:       CardView  = view.findViewById(R.id.card_server_item)
        private val tvFlag:     TextView  = view.findViewById(R.id.tv_flag)
        private val tvCountry:  TextView  = view.findViewById(R.id.tv_country)
        private val tvCity:     TextView  = view.findViewById(R.id.tv_city)
        private val tvPing:     TextView  = view.findViewById(R.id.tv_ping)
        private val tvBadge:    TextView  = view.findViewById(R.id.tv_free_badge)
        private val ivSignal:   ImageView = view.findViewById(R.id.iv_signal)
        private val ivSelected: ImageView = view.findViewById(R.id.iv_selected)

        fun bind(server: ServerItem) {
            tvFlag.text    = server.flag
            tvCountry.text = server.country
            tvCity.text    = server.city
            tvPing.text    = "${server.ping} ms"
            tvBadge.visibility = if (server.isFree) View.VISIBLE else View.GONE

            // Ping color: green < 60ms, yellow < 100ms, red otherwise
            val pingColor = when {
                server.ping < 60  -> R.color.colorConnected
                server.ping < 100 -> R.color.colorConnecting
                else              -> R.color.colorDisconnected
            }
            tvPing.setTextColor(ContextCompat.getColor(itemView.context, pingColor))

            // Signal icon
            val signalIcon = when (server.signalLevel) {
                3    -> R.drawable.ic_signal_full
                else -> R.drawable.ic_signal_medium
            }
            ivSignal.setImageResource(signalIcon)

            // Selected state
            val isSelected = server.id == selectedServerId
            ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE

//            // Card border highlight if selected
//            card.strokeWidth = if (isSelected) 2 else 1

            card.setOnClickListener {
                onServerClick(server)
            }
        }
    }
}
