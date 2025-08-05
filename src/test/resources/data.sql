-- 테스트용 유저
INSERT INTO account (id, username, password, nickname, point, role, win, lose, draw, version)
VALUES
    (1, 'seungho', 'testPassword', '승호', 0, 'USER', 0, 0, 0, 0),
    (2, 'tradeUser', 'testPassword', '교환유저', 0, 'USER', 0, 0, 0, 0)
;

-- 테스트용 카드 시즌
INSERT INTO card_season (id, season_name, image_url)
VALUES
    (1, 'season1', 'season1.png'),
    (2, 'season2', 'season2.png');

-- 테스트용 카드
INSERT INTO card (id, title, attack_type, grade, power, image_url, card_season_id)
VALUES
    -- 시즌 1 카드
    (1, 'card1', 'ALL', 'SSR', 15, 'card1.png', 1),
    (2, 'card2', 'ROCK', 'SR', 13, 'card2.png', 1),
    (3, 'card3', 'SCISSORS', 'SR', 12, 'card3.png', 1),
    (4, 'card4', 'PAPER', 'R', 10, 'card4.png', 1),
    (5, 'card5', 'ROCK', 'R', 11, 'card5.png', 1),
    (6, 'card6', 'SCISSORS', 'N', 7, 'card6.png', 1),
    (7, 'card7', 'PAPER', 'N', 5, 'card7.png', 1),
    -- 시즌 2 카드 CardPackOpen 테스트의 [Unique index or primary key violation]을 최대한 피하기 위해 등급별 3장씩 저장
    (8, 'card8', 'ALL', 'SSR', 15, 'card8.png', 2),
    (9, 'card9', 'ROCK', 'SR', 13, 'card9.png', 2),
    (10, 'card10', 'SCISSORS', 'SR', 12, 'card10.png', 2),
    (11, 'card11', 'PAPER', 'SR', 10, 'card11.png', 2),
    (12, 'card12', 'ROCK', 'R', 11, 'card12.png', 2),
    (13, 'card13', 'SCISSORS', 'R', 7, 'card13.png', 2),
    (14, 'card14', 'PAPER', 'R', 5, 'card14.png', 2),
    (15, 'card15', 'ROCK', 'N', 7, 'card15.png', 2),
    (16, 'card16', 'SCISSORS', 'N', 5, 'card16.png', 2),
    (17, 'card17', 'PAPER', 'N', 5, 'card17.png', 2),
    (18, 'card18', 'ROCK', 'C', 7, 'card18.png', 2),
    (19, 'card19', 'SCISSORS', 'C', 5, 'card19.png', 2),
    (20, 'card20', 'PAPER', 'C', 5, 'card20.png', 2),
    (21, 'card21', 'ROCK', 'V', 7, 'card21.png', 2),
    (22, 'card22', 'SCISSORS', 'V', 5, 'card22.png', 2),
    (23, 'card23', 'PAPER', 'V', 5, 'card23.png', 2);
;

-- 테스트용 Account - Card
INSERT INTO account_card (id, account_id, card_id, count, version, create_at, update_at)
VALUES
    -- account 1이 가진 카드
    (1, 1, 1, 1, 0, now(), now()),
    (2, 1, 3, 1, 0, now(), now()),
    (3, 1, 5, 2, 0, now(), now()),
    (4, 1, 6, 3, 0, now(), now()),

    -- account 2가 가진 카드
    (5, 2, 2, 1, 0, now(), now()),
    (6, 2, 4, 2, 0, now(), now()),
    (7, 2, 8, 1, 0, now(), now()),
    (8, 2, 13, 3, 0, now(), now())
;


-- 테스트용 교환글
INSERT INTO trade_post (id, account_id, title, content, trade_status, grade)
VALUES (1, 1, '교환글 제목', '교환글 내용', 'WAITING', 'SSR');

-- 교환글에 등록된 카드
INSERT INTO trade_post_card (id, trade_post_id, card_id, count)
VALUES
    (1, 1, 1, 1),
    (2, 1, 3, 1),
    (3, 1, 5, 1),
    (4, 1, 6, 1)
;

-- 테스트용 교환신청 글
INSERT INTO trade_request (id, trade_post_id, requester_id, trade_status, version)
VALUES (1, 1, 2, 'WAITING', 0);

-- 신청글에 등록된 카드
INSERT INTO trade_request_card (id, trade_request_id, card_id, count)
VALUES
    (1, 1, 2, 1),
    (2, 1, 4, 1),
    (3, 1, 8, 1),
    (4, 1, 13, 1)
;



