package jpabook.jpashop.repository;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QOrder.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }


//    public List<Order> findAll(OrderSearch orderSearch) {
//
//        return em.createQuery("select o from Order o join o.member m" + " where o.status = :status"
//                + " and m.name like :name", Order.class)
//            .setParameter("status", orderSearch.getOrderStatus())
//            .setParameter("name", orderSearch.getMemberName()).setMaxResults(1000).getResultList();
//    }

    /**
     *jpaCritiria
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
//주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
//회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * QueryDsl
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        JPAQueryFactory query = new JPAQueryFactory(em);

        return query
            .select(order)
            .from(order)
            .join(order.member, member)
            .where(getStatusEq(orderSearch.getOrderStatus()),
                getNameLike(orderSearch.getMemberName()))
            .limit(1000)
            .fetch();

    }

    private BooleanExpression getNameLike(String nameCond) {
        if(!StringUtils.hasText(nameCond)) {
            return null;
        }
        return member.name.like(nameCond);
    }

    private BooleanExpression getStatusEq(OrderStatus statusCond) {
        if(statusCond == null) {
            return null;
        } 
        return order.status.eq(statusCond);
    }

    //    XToOne 관계에 걸리는 애들을 모두 fetch join으로 가져온다. 페이징에 영향을주지 않음
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o"
                    + " join fetch o.member m"
                    + " join fetch o.delivery d", Order.class
            ).setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();

    }

    //    1대 다를 fetchjoin 하는 순간 페이징 불가
//    XToOne 관계는 계속해서 fetch Join 가능
    public List<Order> findAllWithItem() {
        return em.createQuery(
            "select distinct o from Order o"
//order가 같은 id값이면 중복울 제거해준다(Entity가 중복인 경우). db의 distinct와 다름
                + " join fetch o.member m"
                + " join fetch o.delivery d"
                + " join fetch o.orderItems oi"
                + " join fetch oi.item i", Order.class
        ).getResultList();
    }
}
