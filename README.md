# 1.4
유저 이름을 통해 users, refreshToken의 테이블에서 삭제
가독성을 위해 엔드포인트를 유저와 인증으로 나누고
restful api 원칙에 맞춰 엔드포인트 변경

예외를 커스텀하고 GlobalExceptionHandler에서 처리하게 설정
테스트 코드 가독성 좋게 수정 및 개선

# 1.3
https 적용하고 http로 요청시 https로 넘기도록 설정
프로퍼티 파일을 @Value가 아닌 @ConfigurationProperties를 사용하여 클래스를 만들어 받도록 수정
회원 수정 기능 추가
예외, request, contorlle 등 가독성 좋게 수정
자주 사용하는 이름, 비밀번호 상속


# 1.2
로그아웃 구현 및 엑서스 토큰이 받아지지 않는 부분 수정

로그아웃 : https://velog.io/@cs315033/%EC%8A%A4%ED%94%84%EB%A7%81-jwt-%EB%A1%9C%EA%B7%B8%EC%95%84%EC%9B%83-%EA%B5%AC%ED%98%84

토큰 수정 : https://velog.io/@cs315033/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%A6%AC%ED%94%84%EB%A0%88%EC%8B%9C-%ED%86%A0%ED%81%B0%EC%9C%BC%EB%A1%9C-%EC%97%91%EC%84%B8%EC%8A%A4-%ED%86%A0%ED%81%B0-%EA%B0%B1%EC%8B%A0-%EC%88%98%EC%A0%95
