package xm.cloudweight.bean;

import com.xmzynt.storm.service.sort.SortOutData;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wyh
 * @Description: 用来解决分拣撤销数据添加到分拣的列表中
 * @creat 2017/12/22
 */
public class CustomSortOutData extends SortOutData implements Serializable {

    private static final long serialVersionUID = -1943119952657459131L;
    /**
     * :
     * 假设
     * :
     * 你现在有的重量字段名叫W，现在新增一个字段M
     * :
     * 每次分拣的时候，判断是否超过90%，如果不超过，M的值就跟W的值一样，反之，M的值就是剩余的值
     * :
     * 举个例子
     * :
     * 一个采购单5kg
     * :
     * 第一次分拣1kg，不超过90%，这时候W=1，M=1
     * :
     * 第一次分拣完剩余4kg
     * :
     * 第二次分拣也是1kg，还是不超过90%，W=1，M=1
     * :
     * 第二次分拣完剩余3kg
     * :
     * 第三次分拣2.8kg，超过90%（2.7kg），这时候W=2.8，M=3
     * :
     * 一共就有3条记录：
     * 1. W=1  M=1
     * 2. W=1  M=1
     * 3. W=2.8  M=3
     * :
     * 假设要注销第2条记录，这时候先判断对应的采购单验收完了没有，如果没有，就直接在采购单记录中
     * 的剩余的重量上加上撤销的重量W（1kg），这种情况就不需要去管它90%的问题了，因为采购单还没
     * 有验完，不可能有超过90%的分拣记录
     * :
     * 如果之前的采购单已经验收完了（上面举的这个例子），先把该商品的所有分拣记录查找出来（就是上
     * 面的3条记录），分别累加W跟M，sum（W）= 4.8，sum（M）= 5，要注销的记录是第2条的重量W=1，
     * 这时候剩余的重量就是sum（M）- sum（W）+ W[2] = 5 - 4.8 + 1 = 1.2，其中W[2]表示第2条记录中的重量W
     */

    //当分拣量<90%时， 等于分拣数量，   当分拣量>90%时，等于剩下的总量
    private BigDecimal lastWeight;

    public BigDecimal getLastWeight() {
        return lastWeight;
    }

    public void setLastWeight(BigDecimal lastWeight) {
        this.lastWeight = lastWeight;
    }
}
