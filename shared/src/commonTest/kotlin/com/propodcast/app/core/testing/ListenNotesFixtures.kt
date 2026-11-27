package com.propodcast.app.core.testing

/** Recorded Listen Notes responses (copied from ProPodcastN test resources). */
object ListenNotesFixtures {
    fun get(name: String): String = requireNotNull(all[name]) { "Missing fixture $name" }

    private val all: Map<String, String> = mapOf(
        "best_podcasts" to """{
 "id": 93,
 "name": "Business",
 "total": 778,
 "has_next": true,
 "podcasts": [
  {
   "id": "ee84d7d11875465fb89487675ff5425d",
   "rss": "https://feeds.megaphone.fm/WWO2015297742",
   "type": "episodic",
   "extra": {
    "url1": "",
    "url2": "",
    "url3": "",
    "spotify_url": "https://open.spotify.com/show/19TdDBlFkqh7uevYO0jFSW",
    "youtube_url": "",
    "linkedin_url": "",
    "wechat_handle": "",
    "patreon_handle": "",
    "twitter_handle": "EdMylett",
    "facebook_handle": "",
    "amazon_music_url": "",
    "instagram_handle": ""
   },
   "image": "https://cdn-images-3.listennotes.com/podcasts/the-ed-mylett-show-ed-mylett-cumulus-guxpvEVnHTJ-PEUIT9RBhZD.1400x1400.jpg",
   "title": "THE ED MYLETT SHOW",
   "country": "United States",
   "website": "https://www.stitcher.com?utm_source=listennotes.com&utm_campaign=Listen+Notes&utm_medium=website",
   "language": "English",
   "genre_ids": [
    88,
    89,
    90,
    67,
    157,
    171,
    111,
    93,
    181
   ],
   "itunes_id": 1181233130,
   "publisher": "Ed Mylett | Cumulus Podcast Network",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/the-ed-mylett-show-ed-mylett-cumulus-vQDCWVsEFw2-PEUIT9RBhZD.300x300.jpg",
   "is_claimed": false,
   "description": "The Ed Mylett Show showcases the greatest peak-performers across all industries in one place, sharing their journey, kno",
   "looking_for": {
    "guests": false,
    "cohosts": false,
    "sponsors": false,
    "cross_promotion": false
   },
   "has_sponsors": true,
   "listen_score": 74,
   "total_episodes": 391,
   "listennotes_url": "https://www.listennotes.com/c/ee84d7d11875465fb89487675ff5425d/",
   "audio_length_sec": 3083,
   "explicit_content": false,
   "latest_episode_id": "f2cfeaefe882460aa8069b858544d0c2",
   "latest_pub_date_ms": 1713427200000,
   "earliest_pub_date_ms": 1480363465361,
   "has_guest_interviews": true,
   "update_frequency_hours": 83,
   "listen_score_global_rank": "0.01%"
  },
  {
   "id": "4dee7f77872b40a3bd93cd66fc255ec0",
   "rss": "https://feeds.megaphone.fm/TAOC1506947885",
   "type": "episodic",
   "extra": {
    "url1": "",
    "url2": "",
    "url3": "",
    "spotify_url": "https://open.spotify.com/show/08XveTtMJIYffPwjjVwpSM",
    "youtube_url": "",
    "linkedin_url": "",
    "wechat_handle": "",
    "patreon_handle": "",
    "twitter_handle": "theartofcharm",
    "facebook_handle": "",
    "amazon_music_url": "",
    "instagram_handle": ""
   },
   "image": "https://cdn-images-3.listennotes.com/podcasts/the-art-of-charm-the-art-of-charm-xjQJ843Kl2o-ZMBEfuH_irs.1400x1400.jpg",
   "title": "The Art of Charm",
   "country": "United States",
   "website": "https://the-art-of-charm.simplecast.com?utm_source=listennotes.com&utm_campaign=Listen+Notes&utm_medium=website",
   "language": "English",
   "genre_ids": [
    67,
    88,
    90,
    93,
    97,
    107,
    108,
    169,
    111,
    181
   ],
   "itunes_id": 212382281,
   "publisher": "The Art of Charm",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/the-art-of-charm-the-art-of-charm-b90xBszEP6C-ZMBEfuH_irs.300x300.jpg",
   "is_claimed": false,
   "description": "The Art of Charm is where self-motivated people, just like you, come to learn from the company\u2019s coaches about to how to",
   "looking_for": {
    "guests": false,
    "cohosts": false,
    "sponsors": false,
    "cross_promotion": false
   },
   "has_sponsors": true,
   "listen_score": 74,
   "total_episodes": 886,
   "listennotes_url": "https://www.listennotes.com/c/4dee7f77872b40a3bd93cd66fc255ec0/",
   "audio_length_sec": 2980,
   "explicit_content": false,
   "latest_episode_id": "5a204d15541d438789db285208439af9",
   "latest_pub_date_ms": 1713164400000,
   "earliest_pub_date_ms": 1167627600883,
   "has_guest_interviews": true,
   "update_frequency_hours": 167,
   "listen_score_global_rank": "0.01%"
  }
 ],
 "parent_id": 67,
 "page_number": 2,
 "has_previous": true,
 "listennotes_url": "https://www.listennotes.com/best-business-podcasts-93/",
 "next_page_number": 2,
 "previous_page_number": 1
}""",
        "genres" to """{
 "genres": [
  {
   "id": 144,
   "name": "Personal Finance",
   "parent_id": 67
  },
  {
   "id": 151,
   "name": "Locally Focused",
   "parent_id": 67
  },
  {
   "id": 132,
   "name": "Kids & Family",
   "parent_id": 67
  }
 ]
}""",
        "languages" to """{
 "languages": [
  "Any language",
  "Afar",
  "Abkhazian"
 ]
}""",
        "podcast" to """{
 "id": "4d3fe717742d4963a85562e9f84d8c79",
 "rss": "http://sw7x7.libsyn.com/rss",
 "type": "episodic",
 "extra": {
  "url1": "",
  "url2": "",
  "url3": "",
  "spotify_url": "https://open.spotify.com/show/2rQJUP9Y3HxemiW3JHt9WV",
  "youtube_url": "https://www.youtube.com/sw7x7",
  "linkedin_url": "",
  "wechat_handle": "",
  "patreon_handle": "sw7x7",
  "twitter_handle": "",
  "facebook_handle": "sw7x7",
  "amazon_music_url": "",
  "instagram_handle": ""
 },
 "image": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a-daily-bite-sized-dose-of-4UwXJ75eRFg-AIg3cZVKCsL.1400x1400",
 "title": "Star Wars 7x7: A Daily Bite-Sized Dose of Star Wars Joy",
 "country": "United States",
 "website": "https://sw7x7.com?utm_source=listennotes.com&utm_campaign=Listen+Notes&utm_medium=website",
 "episodes": [
  {
   "id": "4e7c59e10e4640b98f2f3cb1777dbb43",
   "link": "https://sites.libsyn.com/55931/864-part-2-of-my-new-conversation-with-bobby-roberts?utm_source=listennotes.com&utm_campa",
   "audio": "https://www.listennotes.com/e/p/4e7c59e10e4640b98f2f3cb1777dbb43/",
   "image": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a/864-part-2-of-my-new-7H2iB0ysPLo-2WVsxtU0f3m.600x315.jpg",
   "title": "864: Part 2 of My (New) Conversation With Bobby Roberts",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a/864-part-2-of-my-new--YcAilqDItR-2WVsxtU0f3m.300x157.jpg",
   "description": "<p>The second half of my latest conversation with Bobby Roberts, Podcast Emeritus from Full of Sith and now Star Wars \"P",
   "pub_date_ms": 1479110402562,
   "guid_from_rss": "bbada2b3a99054ce93b0eb95dd762b4d",
   "listennotes_url": "https://www.listennotes.com/e/4e7c59e10e4640b98f2f3cb1777dbb43/",
   "audio_length_sec": 2447,
   "explicit_content": false,
   "maybe_audio_invalid": false,
   "listennotes_edit_url": "https://www.listennotes.com/e/4e7c59e10e4640b98f2f3cb1777dbb43/#edit"
  },
  {
   "id": "9ae0e2e49a9c477191263df90adf7f3e",
   "link": "https://sites.libsyn.com/55931/863-a-new-conversation-with-bobby-roberts-part-1?utm_source=listennotes.com&utm_campaign=",
   "audio": "https://www.listennotes.com/e/p/9ae0e2e49a9c477191263df90adf7f3e/",
   "image": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a/863-a-new-conversation-with-R0WNdB1xgCV-0YRBTlgiVeU.600x31",
   "title": "863: A (New) Conversation With Bobby Roberts, Part 1",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a/863-a-new-conversation-with-DtJPyYFkpoT-0YRBTlgiVeU.300x15",
   "description": "<p>An in-depth conversation about the Star Wars \"Story\" movies and so much more, featuring Bobby Roberts, Star Wars \"Pod",
   "pub_date_ms": 1479024002563,
   "guid_from_rss": "2c298fe68246aad30bd5afe0b79fdd76",
   "listennotes_url": "https://www.listennotes.com/e/9ae0e2e49a9c477191263df90adf7f3e/",
   "audio_length_sec": 2916,
   "explicit_content": false,
   "maybe_audio_invalid": false,
   "listennotes_edit_url": "https://www.listennotes.com/e/9ae0e2e49a9c477191263df90adf7f3e/#edit"
  }
 ],
 "language": "English",
 "genre_ids": [
  86,
  67,
  68,
  82,
  100,
  101,
  160,
  138
 ],
 "itunes_id": 896354638,
 "publisher": "Star Wars 7x7",
 "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/star-wars-7x7-a-daily-bite-sized-dose-of-qZ2NuyXC4Ix-AIg3cZVKCsL.300x300.j",
 "is_claimed": false,
 "description": "The Star Wars 7x7 Podcast is Rebel-rousing fun for everyday Jedi, generally between 7 and 14 minutes a day, always 7 day",
 "looking_for": {
  "guests": false,
  "cohosts": false,
  "sponsors": false,
  "cross_promotion": false
 },
 "has_sponsors": true,
 "listen_score": 50,
 "total_episodes": 3211,
 "listennotes_url": "https://www.listennotes.com/c/4d3fe717742d4963a85562e9f84d8c79/",
 "audio_length_sec": 582,
 "explicit_content": false,
 "latest_episode_id": "d8713ec794c745fbbb5c33679a63d1d0",
 "latest_pub_date_ms": 1713425400000,
 "earliest_pub_date_ms": 1426143603190,
 "has_guest_interviews": false,
 "next_episode_pub_date": 1478329202571,
 "update_frequency_hours": 24,
 "listen_score_global_rank": "0.5%"
}""",
        "recommendations" to """{
 "recommendations": [
  {
   "id": "7060b5d48b3440ba9668f9af2a90fa7f",
   "rss": "https://www.spreaker.com/show/4920844/episodes/feed",
   "type": "episodic",
   "extra": {
    "url1": "",
    "url2": "",
    "url3": "",
    "spotify_url": "https://open.spotify.com/show/62soqGPhnYJBRrP2vSAk6b",
    "youtube_url": "",
    "linkedin_url": "",
    "wechat_handle": "",
    "patreon_handle": "",
    "twitter_handle": "",
    "facebook_handle": "",
    "amazon_music_url": "",
    "instagram_handle": ""
   },
   "image": "https://cdn-images-3.listennotes.com/podcasts/the-tim-ferriss-show-5-minute-podcast-mpqMa73u08D-mYx8LcSkhz1.1400x1400.jp",
   "title": "The Tim Ferriss Show | 5 minute podcast summaries",
   "country": "United States",
   "website": "https://www.spreaker.com/show/the-tim-ferriss-show-5-minute-podcast-su?utm_source=listennotes.com&utm_campaign=Listen+No",
   "language": "English",
   "genre_ids": [
    111,
    181,
    67
   ],
   "itunes_id": 1556286643,
   "publisher": "5 minute podcast summaries",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/the-tim-ferriss-show-5-minute-podcast-XMuN8q2Jorb-mYx8LcSkhz1.300x300.jpg",
   "is_claimed": false,
   "description": "5 minute summaries of The Tim Ferriss Show's podcast episodes. Get the best insights and ideas in much less time, more a",
   "looking_for": {
    "guests": false,
    "cohosts": false,
    "sponsors": false,
    "cross_promotion": false
   },
   "has_sponsors": false,
   "listen_score": 35,
   "total_episodes": 12,
   "listennotes_url": "https://www.listennotes.com/c/7060b5d48b3440ba9668f9af2a90fa7f/",
   "audio_length_sec": 221,
   "explicit_content": false,
   "latest_episode_id": "9163fe8fdfc34710af8d41b575023e07",
   "latest_pub_date_ms": 1625881044000,
   "earliest_pub_date_ms": 1619229600011,
   "has_guest_interviews": false,
   "update_frequency_hours": 165,
   "listen_score_global_rank": "3%"
  }
 ]
}""",
        "regions" to """{
 "regions": {
  "ae": "United Arab Emirates",
  "al": "Albania",
  "am": "Armenia"
 }
}""",
        "search" to """{
 "count": 1,
 "total": 25,
 "next_offset": 10,
 "took": 0.1,
 "results": [
  {
   "id": "ee84d7d11875465fb89487675ff5425d",
   "title_original": "THE ED MYLETT SHOW",
   "publisher_original": "Ed Mylett | Cumulus Podcast Network",
   "description_original": "The Ed Mylett Show showcases the greatest peak-performers across all industries in one place, sharing their journey, kno",
   "image": "https://cdn-images-3.listennotes.com/podcasts/the-ed-mylett-show-ed-mylett-cumulus-guxpvEVnHTJ-PEUIT9RBhZD.1400x1400.jpg",
   "thumbnail": "https://cdn-images-3.listennotes.com/podcasts/the-ed-mylett-show-ed-mylett-cumulus-vQDCWVsEFw2-PEUIT9RBhZD.300x300.jpg",
   "listennotes_url": "https://www.listennotes.com/c/ee84d7d11875465fb89487675ff5425d/",
   "total_episodes": 391,
   "genre_ids": [
    88,
    89,
    90,
    67,
    157,
    171,
    111,
    93,
    181
   ],
   "latest_pub_date_ms": 1713427200000,
   "explicit_content": false,
   "title_highlighted": "x",
   "rss": "https://feeds.megaphone.fm/WWO2015297742"
  }
 ]
}""",
    )
}
