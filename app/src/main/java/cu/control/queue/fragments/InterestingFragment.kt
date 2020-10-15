package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterInteresting
import cu.control.queue.interfaces.OnInterestingClickListener
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.InterestingClient
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_ADD_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_CHECKED
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_LAST_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_MEMBER_UPDATED_DATE
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NAME
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_NUMBER
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_PRODUCTS
import cu.control.queue.repository.dataBase.entitys.payload.Person.Companion.KEY_REINTENT_COUNT
import cu.control.queue.utils.ColorGenerator
import cu.control.queue.utils.Common.Companion.drawProductBackground
import cu.control.queue.utils.Common.Companion.getStoreName
import cu.control.queue.utils.Conts.Companion.formatDateMid
import cu.control.queue.utils.Conts.Companion.formatDateOnlyTime
import kotlinx.android.synthetic.main.dialog_products.view.*
import kotlinx.android.synthetic.main.interesting_fragment.*
import kotlinx.android.synthetic.main.item_product.view.*
import me.yokeyword.fragmentation.SupportFragment

class InterestingFragment(val person: Person, val client: Client) : SupportFragment(),
    OnInterestingClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.interesting_fragment, null)

        setHasOptionsMenu(true)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        val adapter = AdapterInteresting(this)
        recycler_view.layoutManager = LinearLayoutManager(requireContext())

        adapter.contentList = prepareList()

        recycler_view.adapter = adapter
    }

    @SuppressLint("RestrictedApi")
    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom_white)

            title = client.name

            setNavigationOnClickListener {
                pop()
            }
        }
    }

    private fun prepareList(): ArrayList<InterestingClient> {

        val list = ArrayList<InterestingClient>()

        person.info.let {
            it.map { item ->
                when (item.key) {
                    KEY_ADD_DATE -> {
                    }
                    KEY_REINTENT_COUNT -> {
                    }
                    KEY_CHECKED -> {
                    }
                    KEY_LAST_NAME -> {
                    }
                    KEY_NAME -> {
                    }
                    KEY_NUMBER -> {
                    }
                    KEY_MEMBER_UPDATED_DATE -> {
                    }
                    else -> {
                        if (item.value is MutableMap<*, *>) { //las colas del store
                            val storeName = getStoreName(item.key, requireContext())
                            (item.value as MutableMap<*, *>).map { queue -> //atributos de las colas
                                val value = queue.value as MutableMap<*, *>
                                val day = formatDateMid.format(value[KEY_ADD_DATE] as Double)
                                val hour = formatDateOnlyTime.format(value[KEY_ADD_DATE] as Double)
                                val name = value[KEY_NAME] as String
                                val number = (value[KEY_NUMBER] as Double).toInt().toString()
                                val products = value[KEY_PRODUCTS] as ArrayList<String>
                                list.add(
                                    InterestingClient(number, storeName, day, hour, name, products)
                                )
                            }
                        }
                    }
                }
            }
        }

        return list
    }

    override fun onClick(client: InterestingClient) {
        val dialog = AlertDialog.Builder(requireContext(), R.style.RationaleDialog).create()
        dialog.setView(getProductsView(client, dialog))
        dialog.show()
    }

    private fun getProductsView(client: InterestingClient, dialog: AlertDialog): View {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_products, null)

        dialogView.flexBox_products.removeAllViews()

        dialogView.saveButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.textViewTitle.text = client.queueName

        client.products.map { product ->
            val view = inflater.inflate(R.layout.item_product, null)
            view.textViewProduct.text = product
            val color = ColorGenerator.MATERIAL.getColor(product)
            val shadow =
                ContextCompat.getColor(requireContext(), R.color.gray_transparent)
            view.layoutProduct.background = drawProductBackground(color)
            view.layoutProductShadow.background = drawProductBackground(shadow)

            dialogView.flexBox_products.addView(view)
        }

        return dialogView
    }
}