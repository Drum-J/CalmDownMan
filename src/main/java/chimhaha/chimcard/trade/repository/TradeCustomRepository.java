package chimhaha.chimcard.trade.repository;


import chimhaha.chimcard.entity.Grade;
import chimhaha.chimcard.entity.TradeStatus;
import chimhaha.chimcard.trade.dto.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static chimhaha.chimcard.entity.QAccount.account;
import static chimhaha.chimcard.entity.QCard.card;
import static chimhaha.chimcard.entity.QTradePost.tradePost;
import static chimhaha.chimcard.entity.QTradePostCard.tradePostCard;
import static chimhaha.chimcard.entity.QTradeRequest.tradeRequest;
import static chimhaha.chimcard.entity.QTradeRequestCard.tradeRequestCard;

@Repository
@RequiredArgsConstructor
public class TradeCustomRepository {

    private final JPAQueryFactory query;

    public Page<TradePostListDto> getPostList(PageRequest pageRequest, TradeStatus status, Grade grade) {
        List<TradePostListDto> postList = query
                .select(new QTradePostListDto(
                        tradePost.id,
                        tradePost.title,
                        tradePost.content,
                        tradePost.tradeStatus,
                        tradePost.grade,
                        account.username,
                        account.nickname,
                        account.profileImage,
                        tradePost.id.count().as("cardCount")
                ))
                .from(tradePost)
                .join(account)
                    .on(tradePost.owner().eq(account))
                .join(tradePostCard)
                    .on(tradePost.id.eq(tradePostCard.tradePost().id))
                .where(statusEq(status), gradeEq(grade))
                .groupBy(tradePost.id)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .orderBy(tradePost.id.desc())
                .fetch();

        JPAQuery<Long> totalCount = query.select(tradePost.count())
                .from(tradePost)
                .where(statusEq(status), gradeEq(grade));

        return PageableExecutionUtils.getPage(postList, pageRequest, totalCount::fetchOne);
    }

    public List<TradeCardDetailDto> getPostDetail(Long tradePostId) {
        return query
                .select(new QTradeCardDetailDto(
                        card.id,
                        card.title,
                        card.grade,
                        tradePostCard.count,
                        card.imageUrl
                ))
                .from(tradePostCard)
                .join(card)
                    .on(tradePostCard.card().eq(card))
                .where(tradePostCard.tradePost().id.eq(tradePostId))
                .fetch();
    }

    public List<TradeRequestListDto> getRequestList(Long tradePostId) {
        return query
                .select(new QTradeRequestListDto(
                        tradeRequest.id,
                        account.id,
                        account.username,
                        account.nickname,
                        account.profileImage,
                        tradeRequest.tradeStatus,
                        tradeRequest.id.count().as("cardCount")
                ))
                .from(tradeRequest)
                .join(account)
                .on(tradeRequest.requester().eq(account))
                .join(tradePost)
                    .on(tradePost.id.eq(tradeRequest.tradePost().id),
                            tradePost.id.eq(tradePostId))
                .join(tradeRequestCard)
                    .on(tradeRequest.id.eq(tradeRequestCard.tradeRequest().id))
                .groupBy(tradeRequest.id)
                .fetch();
    }

    public List<TradeCardDetailDto> getRequestDetail(Long tradeRequestId) {
        return query
                .select(new QTradeCardDetailDto(
                        card.id,
                        card.title,
                        card.grade,
                        tradeRequestCard.count,
                        card.imageUrl
                ))
                .from(tradeRequestCard)
                .join(card)
                    .on(tradeRequestCard.card().eq(card))
                .where(tradeRequestCard.tradeRequest().id.eq(tradeRequestId))
                .fetch();
    }

    private BooleanExpression statusEq(TradeStatus status) {
        return status != null ? tradePost.tradeStatus.eq(status) : null;
    }

    private BooleanExpression gradeEq(Grade grade) {
        return grade != null ? tradePost.grade.eq(grade) : null;
    }
}
