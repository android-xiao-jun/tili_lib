package androidx.lifecycle

/**
 * desc:
 * verson:
 * create by zj on 2025/07/07 09:26
 * update by zj on 2025/07/07 09:26
 */
object Transformations2 {

    @JvmStatic
    fun <X, Y> LiveData<X>.map(source: LiveData<X>, transform: (X) -> Y): LiveData<Y> {
        return source.map(transform)
    }

    @JvmStatic
    fun <X, Y> switchMap(source: LiveData<X>, switchMapFunction: (X) -> LiveData<Y>): LiveData<Y> {
        return source.switchMap(switchMapFunction)
    }
    @JvmStatic
    fun <X> distinctUntilChanged(source:LiveData<X>): LiveData<X> {
        return source.distinctUntilChanged()
    }
}