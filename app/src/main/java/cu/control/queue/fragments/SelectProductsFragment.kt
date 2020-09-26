package cu.control.queue.fragments

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterProducstToAdd
import cu.control.queue.repository.dataBase.entitys.Product
import cu.control.queue.repository.dataBase.entitys.payload.Person
import cu.control.queue.repository.dataBase.entitys.payload.params.Param
import cu.control.queue.utils.PreferencesManager
import cu.control.queue.viewModels.ClientViewModel
import kotlinx.android.synthetic.main.fragment_select_products.*
import kotlinx.android.synthetic.main.layout_add_product_dialog.view.*
import kotlinx.android.synthetic.main.layout_add_product_dialog.view.productName
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import me.yokeyword.fragmentation.SupportFragment

class SelectProductsFragment(
    private val clientViewModel: ClientViewModel
) : SupportFragment() {

    val adapter = AdapterProducstToAdd()
    lateinit var preferences: PreferencesManager
    lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.fragment_select_products, null)

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        preferences = PreferencesManager(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolBar()

        view._okButton.setOnClickListener {
            dialogAddProduct()
        }
        view._okButtonSave.setOnClickListener {
            saveChanges()
            pop()
        }

        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        var list = preferences.getProducts()

        clientViewModel.creatingQueue.value!!.info!![Param.KEY_QUEUE_PRODUCTS]?.let { arrayList ->
            if ((arrayList as ArrayList<*>).isNotEmpty()) {
                if (list.isNotEmpty()) {
                    val temList = list.map {
                        it.ischeck = arrayList.contains(it.name)
                        it
                    }.toList()

                    if (list.size > 1) {
                        list = temList as ArrayList<Product>
                    } else {
                        list = arrayListOf(temList[0])
                    }
                }

                arrayList.map { productName ->
                    productName as String
                    if (list.find { it.name == productName } == null) {
                        list.add(Product(productName, true))
                    }
                }
            }
        }

        adapter.contentList = list
        recycler_view.adapter = adapter
    }

    private fun initToolBar() {
        with(toolbar as androidx.appcompat.widget.Toolbar) {

            setNavigationIcon(R.drawable.ic_back_custom)

            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

            title = "Productos"

            setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))

            setNavigationOnClickListener {
                saveChanges()
                pop()
            }
        }
    }

    private fun saveChanges() {
        val queue = clientViewModel.creatingQueue.value!!
        val list = ArrayList<String>()
        adapter.contentList.map {
            if (it.ischeck)
                list.add(it.name)
        }
        (queue.info as MutableMap)[Person.KEY_PRODUCTS] = list

        clientViewModel.creatingQueue.postValue(queue)
        val listToSave = adapter.contentList.map {
            it.ischeck = false
            it
        }.toList()
        preferences.setProducts(
            when {
                listToSave.isEmpty() -> ArrayList()
                listToSave.size == 1 -> arrayListOf(
                    listToSave[0]
                )
                else -> listToSave as ArrayList<Product>
            }
        )
    }

    override fun onBackPressedSupport(): Boolean {
        saveChanges()
        return super.onBackPressedSupport()
    }

    private fun dialogAddProduct() {
        dialog = AlertDialog.Builder(requireContext(), R.style.RationaleDialog)

            .setView(getDialogView())
            .create()

        dialog.show()
    }

    private fun getDialogView(): View {
        val view = View.inflate(context, R.layout.layout_add_product_dialog, null)
        view.saveButton.setOnClickListener {
            val text = view.productName.text.trim().toString()
            if (text.isNotEmpty()) {
                if (adapter.contentList.count { it.name == text } > 0) {
                    showError("El producto ya está en la lista.")
                } else {
                    adapter.contentList.add(Product(text))
                    adapter.contentList.sortBy { it.name }
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
            } else {
                showError("Inserte un nombre de producto.")
            }
        }

        return view
    }

    private fun showError(error: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
}