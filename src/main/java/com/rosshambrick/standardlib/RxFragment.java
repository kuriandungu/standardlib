package com.rosshambrick.standardlib;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;

@SuppressWarnings("UnusedDeclaration")
public abstract class RxFragment extends DialogFragment {

    private BlockingProgressFragment blockingProgressFragment;
    private Toolbar toolbar;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    abstract protected boolean isDebug();

    protected void showBlockingProgress() {
        showBlockingProgress(null);
    }

    protected void showBlockingProgress(Subscription subscription) {
        blockingProgressFragment = BlockingProgressFragment.newInstance();
        blockingProgressFragment.show(getFragmentManager(), BlockingProgressFragment.TAG);
        blockingProgressFragment.setOnCancelListener(subscription);
    }

    public void handleError(Throwable e) {
        handleError(null, e);
    }

    protected void handleError(String message, Throwable e) {
        Log.e(getClass().getSimpleName(), e.getLocalizedMessage(), e);
        if (isDebug() || message != null) {
            Toast.makeText(getActivity(), message == null ? e.getLocalizedMessage() : message, Toast.LENGTH_LONG).show();
        }
    }

    protected void dismissBlockingProgress() {
        if (blockingProgressFragment != null) {
            blockingProgressFragment.dismiss();
            blockingProgressFragment = null;
        }
    }

    public Toolbar getToolbar() {
        if (toolbar == null) {
            throw new RuntimeException("Toolbar has not been set.  Make sure not to call getToolbar() until onViewCreated() at the earliest.");
        }
        return toolbar;
    }

    //TODO: move this to a util class
    protected <T> Subscription blockingSubscribe(Observable<T> observable, final Observer<T> observer) {
        Subscription subscription = bind(observable
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        dismissBlockingProgress();
                    }
                })
        )
                .subscribe(observer);
        showBlockingProgress(subscription);
        return subscription;
    }

    @Deprecated
    protected <T> Subscription subscribe(Observable<T> observable, Observer<T> observer) {
        return bind(observable).subscribe(observer);
    }

    @Deprecated
    protected <T> Observable<T> bind(Observable<T> observable) {
        return observable.compose(Rx.<T>bind(this));
//        return LifecycleObservable.bindFragmentLifecycle(lifecycle(), boundObservable);
    }

    //TODO: move this to a util class
    protected void toast(int messageId) {
        ToastUtil.show(getActivity(), messageId);
    }

    //TODO: move this to a util class
    protected void toast(String message) {
        ToastUtil.show(getActivity(), message);
    }

    protected int getColor(int colorRes) {
        return getResources().getColor(colorRes);
    }

    //TODO: move this to a util class
    protected int getDimensionPixelOffset(int dpResource) {
        return getResources().getDimensionPixelOffset(dpResource);
    }

    //TODO: move this to a util class
    protected void debugToast(int messageResId) {
        if (isDebug()) {
            Toast.makeText(getActivity(), messageResId, Toast.LENGTH_SHORT).show();
        }
    }

    public void onCompleted() {
        //nothing by default
    }

    public void onError(Throwable e) {
        handleError(e);
    }

}
