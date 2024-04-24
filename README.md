# 🗺 오노프


## ✨ 프로젝트 소개

사용자 위치 기반으로 같은 장소에 있는 사람들과 채팅, 같은 동네 사람들과 소통하는 SNS 앱.

[<img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/cd58e095-50e9-40c8-b055-e375d282186c" height="100">](https://play.google.com/store/apps/details?id=com.anshyeon.onoff)


## 💡 프로젝트 동기

오늘날에는 오프라인에서 가까이 있는 사람들보다 온라인으로 멀리 떨어져 있는 사람들과 더 많은 정보를 주고 받고 있습니다. 

공연장, 테마파크, 아파트 단지 등 같은 장소에 있다는 것은 같은 목적을 가지고 있는 것이라 생각했습니다.

그 누구보다 서로가 필요한 정보를 가진 사람들이 서로 소통할 수 있는 프로젝트를 구상했습니다.

## 📚 기술스택

### Android
| Category  | TechStack |
| ------------- | ------------- |
| Architecture  | <img src="https://img.shields.io/badge/MVVM-603B2C"> | 
| Jetpack | <img src="https://img.shields.io/badge/ViewModel-28456C"> <img src="https://img.shields.io/badge/LiveData-5A5A5A"> <img src="https://img.shields.io/badge/DataBinding-373737"> <img src="https://img.shields.io/badge/Navigation-89632A"> | 
| Network | <img src="https://img.shields.io/badge/Retrofit-603B2C"> <img src="https://img.shields.io/badge/OkHttp3-492F64">| 
| Asynchronous | <img src="https://img.shields.io/badge/Coroutine-69314C"> <img src="https://img.shields.io/badge/Flow-89632A"> | 
| DI | <img src="https://img.shields.io/badge/Hilt-2B593F"> | 
| Image | <img src="https://img.shields.io/badge/Coil-28456C"> | 
| Local DB |<img src="https://img.shields.io/badge/Jetpack Room-492F64"> | 
| Json | <img src="https://img.shields.io/badge/Moshi-854C1D"> | 
| Open API | <img src="https://img.shields.io/badge/Naver Map API-69314C"> | 

## 💬 배운점

1. MVVM 패턴 적용
   - Fragment와 Activity에 모든 코드들이 위치해 기능이 추가되면서 유지보수성을 고려해 MVVM 패턴을 적용했습니다.
   - 처음 아키텍처를 적용하면서 UI 레이어에는 직접 UI를 나타내는 코드만 존재해야 한다고 생각했는데, ViewModel을 구현하면서 UI를 나타내기 위한 책임을 가지는 코드들이 모두 UI레이어에 속속한다는 것 확실히 이해했습니다.
   - 계층을 분리해 캡슐화를 이뤄 네트워크 통신 기능을 수정할 때 Data레이어만 수정하여 유지보수성 향상을 경험했습니다.
    
 
2. 입력 이벤트 중복처리 방지
   - 게시물 작성 시 완료버튼 클릭 횟수만큼 처리되는 문제를 확인했습니다.
   - Debounce와 Throttle 연산자의 차이를 확인하며, 일정 시간동안 들어온 값 중에서 가장 첫번째 값만 발행하고 나머지는 무시하는ThrottleFirst() 연산자가 필요하다고 생각했습니다.
   - RxJava는 throttleFirst() operator가 있지만 Flow에서는 지원하지 않아 Flow의 확장함수로 throttleFirst를 구현해 프로젝트에서 입력 이벤트의 중복처리를 방지했습니다.
 
3. 네이버와 카카오의 open API
   - 지도 API
     - 프로젝트를 구현하며 여러 장소에서 테스트를 해야하는데, 에뮬레이터의 현재 위치를 설정할 수 있어 직접 디바이스를 가지고 오프라인에서 직접 이동하지 않아도 테스트가 가능했습니다.
     - 그런데 카카오 지도는 윈도우 환경의 에뮬레이터에서 사용하지 못해 에뮬레이터에서 사용 가능한 네이버 지도로 선택했습니다.
    - 장소 검색 API
      - 네이버 지역 검색 API와 카카오 로컬 API 모두 장소명, 도로명 주소, 지번 주소, 좌표를 응답 받을 수 있습니다.
      - 하지만 네이버 지역 검색 API는 특정 좌표를 기준으로 지정된 거리 내에서 키워드 검색을 할 수 없다.
      - 네이버 지도를 사용하기 때문에 네이버 지역 검색 API를 사용하면 같은 플랫폼의 open API를 사용하는 이점이 있지만 사용자의 위치를 기준으로 검색이 가능해야 하기에 카카오 로컬 API로 선택했다.

4. Moshi, @Json을 사용해 사용자 정의 이름 지정
   - 프로젝트의 변수 네이밍 컨벤션을 camelCase로 선언하는데, kakao local REST API의 응답으로 받은 값의 field가 snake_case로 선언 되어있습니다.
   - Moshi를 사용해 역질렬화 하기 위해 이를 매핑 해주는 기능을 구글링 했는데, StackOverFlow에서 해당 기능을 지원하지 않는다는 답변을 확인했습다.
   - 만약 공식문서를 확인하지 않고 StackOverFlow의 답변을 믿었다면 data class를 선언할때 파라미터들을 snake_case로 선언했을 것입니다.
   - 하지만 공식문서를 한번 더 확인해 봤을때 @Json 에노테이션을 사용해 매핑할수 있다는 내용을 확인했습니다.
   - 이전에도 블로그나 StackOverFlow에 있는 정보를 그대로 믿으면 안된다는 생각은 하고 있었으나 이번 경험을 통해 공식문서를 먼저 확인해야겠다고 생각했습니다.




## 📺︎ 작동화면

<div align="center">

| 로그인 | 정보 입력 | 로그아웃 |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/ffa417a2-5631-4f30-bc42-19088f948438" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/a71d26b9-bce7-4f15-917e-952e62ce7307" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/c42d663e-9667-4518-9812-78ebd5b32672" align="center" width="250px"/> |

</div>

<div align="center">

| 장소검색 | 채팅방 입장 | 실시간 채팅 |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/ccaae820-66a6-4e0f-92e6-6f423b0cf65e" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/18b3c133-1d08-4353-bf0a-62afbe8bc46a" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/fee2e3bd-4172-4810-96b3-1f39a9b1ad61" align="center" width="250px"/> |

</div>

<div align="center">

| 프로필 수정 | 프로필 수정 반영 | 게시물 작성 |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/70b7a408-95ed-42c0-bb15-8c2cf8528eff" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/731e4484-e84b-4fa0-a60b-05c3743df992" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/b6b18b3e-20bc-4cbe-99cf-e3e7c2e129d3" align="center" width="250px"/> |

</div>

<div align="center">

| 게시물 상세 화면 | 오프라인 모드 | 다크 모드 |
| :---------------: | :---------------: | :---------------: |
| <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/6ecd1c70-7539-4e36-8424-8b55f6c74227" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/b8c5d4b1-1a5b-4746-b71d-f8c4b36ae97a" align="center" width="250px"/> | <img src="https://github.com/ANSHyeon/ONOFF/assets/127817240/f8306648-01e1-474c-8f26-842027d35a57" align="center" width="250px"/> |

</div>



## 📑 관련 자료
[FIgma 바로가기](https://www.figma.com/file/CJkEYaXwFzd4RP4XKajX3S/on%2Foff?type=design&node-id=0%3A1&mode=design&t=PzZcnU6pSpx9dvfx-1)

[프로젝트 계획서 바로가기](https://docs.google.com/spreadsheets/d/1rufP5iTGT0CtReWxhzR3FXR4tQlngI9rBvHU_P8Yom8/edit?usp=sharing)


