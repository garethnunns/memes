<?php
	// these are the functions that are only used on the website
	// they build upon the core functions:
	require_once dirname(__FILE__).'/functions.php';

	function check() {
		// checks the user is logged in
		// redirects them to the home page if not

		if(!loggedIn()) { // user not logged in
			header("Location: /?goingto=".filter_var($_SERVER['REQUEST_URI'], FILTER_SANITIZE_URL));
			die('Please login'); //stop the rest of the script executing
		}
	}

	function loggedIn() {
		// returns bool - true if logged in

		if(!isset($_SESSION['user']) || !isset($_SESSION['key']))
			return false;
		if(($user = userDetails($_SESSION['key'])) === false) { // couldn't find user with that ket
			session_destroy();
			die('The account you are logged in on no longer exists');
		}
		return true;
	}

	function displayMeme($meme) {
		// expects a $meme like those in the $memes array produced by a function like memeFeed()

		echo "
		<div class='memeContainer'>
			<div class='meme-header'>
				<img src='{$meme['poster']['pic']}' alt='{$meme['poster']['username']} profile picture' class='pp'/>
				<div class='meme-ago'>
					<a href='{$meme['link']}' title='Go to meme'>{$meme['time']['ago']}</a>
				</div>
				<h3>".(($meme['original']) ? '<span class="icon-repost"></span> Reposted by' : '')."
					<a href='{$web}{$meme['poster']['username']}' title='{$meme['poster']['name']}'>
						{$meme['poster']['username']}
					</a>
				</h3>
				<p>".($meme['original'] ? // when there is an original of that post (it's a repost)
					"<a href='{$meme['original']['link']}' title='Go to original post'>Originally posted</a> by 
					<a href='{$meme['original']['poster']['link']}' title='{$meme['original']['poster']['name']}'>
						{$meme['original']['poster']['username']}
					</a>" : // when it's an original
					"Posted by {$meme['poster']['name']}")."</p>
			</div>
			<img src='{$meme['images']['full']}' class='meme' alt='Meme ".($meme['original'] ? 'reposted' : 'posted')." by {$meme['poster']['username']}'>
			<p class='meme-comment'>
			<img src='{$meme['poster']['pic']}' alt='{$meme['poster']['username']} profile picture' class='pp'/>
			".htmlspecialchars($meme['caption'])."</p>
			<div class='meme-actions'>
				<div><span class='icon-comment'></span><br>{$meme['comments-num']} comments</div>
				<div><span class='icon-star-empty'></span><br>{$meme['stars-num']} stars</div>
				<div><span class='icon-repost'></span><br>{$meme['reposts-num']} reposts</div>
			</div>";
		if($meme['comments-num'] > 5)
			echo "<a href='{$meme['link']}' title='Go to post'>View all comments&hellip;</a>";
		if($meme['comments-num'] && $meme['comments']) {
			echo "<div class='meme-comments'>";
			foreach($meme['comments'] as $comment)
				echo "
				<div class='meme-comment'>
					<h4 class='meme-comment-name'>
						<div class='meme-ago'>{$comment['time']['ago']}</div>
						<a href='{$comment['commenter']['link']}' title='{$comment['commenter']['name']}'>
							<img src='{$comment['commenter']['pic']}' class='pp' alt='{$meme['poster']['username']} profile picture' /> {$comment['commenter']['username']}
						</a>
					</h4>
					<p>".htmlspecialchars($comment['comment'])."</p>
				</div>";
			echo "</div>";
		}
		echo "</div>";
	}
?>