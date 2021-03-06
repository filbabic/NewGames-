package com.babic.filip.coreui.base

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.babic.filip.coreui.common.subscribe
import com.babic.filip.coreui.scope.ScopeRetainer
import com.babic.filip.coreui.scope.ScopeRetainerFactory
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

abstract class BaseFragment<Data : Any> : Fragment(), BaseView {

    private val scopeRetainer: ScopeRetainer by lazy { buildScopeRetainer() }
    protected val scope: Scope
        get() = scopeRetainer.scope

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeScope(savedInstanceState)
        getPresenter().viewReady(this)

        val activity = activity as? BaseActivity<*>
        activity?.run { initPresenter(this) }
        subscribeToViewState()
    }

    private fun subscribeToViewState() {
        getPresenter().viewState().subscribe(this, ::onViewStateChanged)
    }

    abstract fun onViewStateChanged(viewState: Data)

    private fun initializeScope(savedInstanceState: Bundle?) {
        savedInstanceState ?: scopeRetainer.initializeScope(getScope())
    }

    private fun initPresenter(baseActivity: BaseActivity<*>) {
        getPresenter().setRoutingSource(get(parameters = { parametersOf(baseActivity) }))
    }

    //override to provide extra behaviour for error handling, leave it as is when you don't need to handle certain errors
    override fun showAuthenticationError() = Unit

    override fun showNetworkError() = Unit
    override fun showParseError() = Unit
    override fun showServerError() = Unit

    override fun onDestroy() {
        val baseViewModel = getPresenter() as? BasePresenter<*, *>
        baseViewModel?.onDestroy()

        super.onDestroy()
    }

    abstract fun getPresenter(): StatePresenter<Data, BaseView>

    @LayoutRes
    abstract fun getLayout(): Int

    abstract fun getScope(): String

    private fun buildScopeRetainer(): ScopeRetainer = ViewModelProviders.of(this, getScopeFactory()).get(ScopeRetainer::class.java)

    private fun getScopeFactory() = ScopeRetainerFactory()
}