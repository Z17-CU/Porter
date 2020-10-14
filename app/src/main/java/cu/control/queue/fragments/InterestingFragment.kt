package cu.control.queue.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cu.control.queue.R
import cu.control.queue.adapters.AdapterInteresting
import cu.control.queue.repository.dataBase.entitys.Client
import cu.control.queue.repository.dataBase.entitys.payload.Person
import kotlinx.android.synthetic.main.interesting_fragment.*
import me.yokeyword.fragmentation.SupportFragment

class InterestingFragment(val person: Person, val client: Client) : SupportFragment() {

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

        val adapter = AdapterInteresting()
        recycler_view.layoutManager = LinearLayoutManager(requireContext())



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
}