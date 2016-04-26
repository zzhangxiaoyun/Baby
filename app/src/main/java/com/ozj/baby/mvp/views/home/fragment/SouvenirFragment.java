package com.ozj.baby.mvp.views.home.fragment;

import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.ozj.baby.R;
import com.ozj.baby.adapter.SouvenirAdapter;
import com.ozj.baby.base.BaseFragment;
import com.ozj.baby.event.AddSouvenirEvent;
import com.ozj.baby.mvp.model.bean.Souvenir;
import com.ozj.baby.mvp.model.rx.RxBabyRealm;
import com.ozj.baby.mvp.model.rx.RxBus;
import com.ozj.baby.mvp.presenter.home.impl.SouvenirPresenterImpl;
import com.ozj.baby.mvp.views.home.ISouvenirVIew;
import com.ozj.baby.mvp.views.home.activity.AddSouvenirActivity;
import com.ozj.baby.mvp.views.home.activity.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observer;
import rx.Subscription;

/**
 * Created by Administrator on 2016/4/19.
 */
public class SouvenirFragment extends BaseFragment implements ISouvenirVIew, SwipeRefreshLayout.OnRefreshListener {
    @Inject
    SouvenirPresenterImpl mSouvenirPresenterImpl;
    @Inject
    RxBabyRealm mRxBabyRealm;
    @Inject
    RxBus mRxbus;
    SouvenirAdapter mAdapter;
    @Bind(R.id.ry_souvenir)
    RecyclerView rySouvenir;
    @Bind(R.id.swipeFreshLayout)
    SwipeRefreshLayout swipeFreshLayout;
    Subscription mSubscription;

    int page = 0;
    int size = 15;
    LinearLayoutManager layout;
    List<Souvenir> mList;
    boolean isFirst = true;

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_main;
    }

    @Override
    public void initViews() {
        layout = new LinearLayoutManager(getActivity());
        swipeFreshLayout.setOnRefreshListener(this);
        rySouvenir.setLayoutManager(layout);
        rySouvenir.setItemAnimator(new DefaultItemAnimator());
        swipeFreshLayout.setColorSchemeResources(R.color.colorPrimary);
        Logger.init(this.getClass().getSimpleName());
    }

    public static SouvenirFragment newInsatance() {
        return new SouvenirFragment();

    }

    @Override
    public void initDagger() {
        mFragmentComponet.inject(this);

    }

    @Override
    public void initData() {
        mSouvenirPresenterImpl.LoadingDataFromNet(true, size, 0);
        mSubscription = mRxbus.toObservable(AddSouvenirEvent.class)
                .subscribe(new Observer<AddSouvenirEvent>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast("出错啦，请稍后再试");
                        Logger.e(e.getMessage());

                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNext(AddSouvenirEvent addSouvenirEvent) {
                        if (isFirst) {
                            if (addSouvenirEvent.isList()) {
                                mList = addSouvenirEvent.getMlist();
                            } else {
                                mList = new ArrayList<>();
                                mList.add(addSouvenirEvent.getSouvenir());
                            }
                            mAdapter = new SouvenirAdapter(mList, getActivity());
                            rySouvenir.setAdapter(mAdapter);
                            rySouvenir.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING && layout.findLastVisibleItemPosition() + 1 == mAdapter.getItemCount()) {
                                        swipeFreshLayout.setRefreshing(true);
                                        page++;
                                        mSouvenirPresenterImpl.LoadingDataFromNet(false, size, page);
                                    }
                                }
                            });
                            isFirst = false;
                        } else {
                            if (addSouvenirEvent.isList()) {
                                if (addSouvenirEvent.isRefresh()) {
                                    for (Souvenir s : addSouvenirEvent.getMlist()) {
                                        if (!mList.contains(s)) {
                                            mList.add(0, s);
                                            mAdapter.notifyItemInserted(0);
                                        }
                                    }

                                } else {
                                    for (Souvenir s : addSouvenirEvent.getMlist()) {
                                        if (!mList.contains(s)) {
                                            mList.add(s);
                                            mAdapter.notifyItemInserted(mList.size());
                                        }
                                    }

                                }
                            } else {
                                if (!mList.contains(addSouvenirEvent.getSouvenir())) {
                                    mList.add(0, addSouvenirEvent.getSouvenir());
                                    mAdapter.notifyItemInserted(0);
                                }
                            }
                        }

                    }
                });

    }


    @Override
    public void initToolbar() {
        ActionBar toolbar = getBaseActivity().getSupportActionBar();
        if (toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void initPresenter() {
        mSouvenirPresenterImpl.attachView(this);
    }


    @Override
    public void showRefreshingLoading() {
        swipeFreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeFreshLayout != null)
                    swipeFreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideRefreshingLoading() {
        if (swipeFreshLayout != null)
            swipeFreshLayout.setRefreshing(false);
    }

    @Override
    public void toAddNewSouvenirActivity() {
        Intent intent = new Intent(getActivity(), AddSouvenirActivity.class);
        startActivity(intent);
    }

    @Override
    public void ToProFileActivity() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mSouvenirPresenterImpl.LoadingDataFromNet(true, size, 0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscription != null) {
            mSubscription.isUnsubscribed();
        }
    }

}