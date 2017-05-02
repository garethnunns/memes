<?php
	session_set_cookie_params(6*60*60); // clients remember sessions for 6 hours
	session_start();

	// these are the functions that are only used on the website
	// they build upon the core functions:
	require_once dirname(__FILE__).'/functions.php';

	$pageError = "
	<div class='pageError'>
		<h1>4<span class='icon-star-empty'></span>4</h1>
		<h3>Unfortunately we couldn't find that page &ndash; it might have been removed or just broken</h3>
		<p>If you think there's a error let us know, but you can always <a href='".$web."'>return to safety for now</a></p>
	</div>";

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

	function userDetailsFromUsername($username) {
		// like user details, only it expects a string $username

		global $dbh; // database connection

		try {
			$sql = "SELECT user.*
					FROM user
					WHERE  BINARY username = ?
					AND emailcode IS NULL";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($username)); // sanitise user input

			if($sth->rowCount()==0 || $sth->rowCount()>1) return false;

			return $sth->fetch(PDO::FETCH_OBJ);
		}
		catch (PDOException $e) {
			return false;
		}
	}

	function displayMeme($meme) {
		// expects a $meme like those produced by the meme()['meme'] function

		global $web; // to link to the list pages

		echo "
		<div class='memeContainer'>
			<div class='meme-header'>
				<img src='{$meme['poster']['pic']}' alt='{$meme['poster']['username']} profile picture' class='pp'/>
				<div class='meme-ago'>
					<a href='{$meme['link']}' title='Go to meme'>{$meme['time']['ago']}</a>
				</div>
				<h3>".(($meme['original']) ? '<span class="icon-repost"></span><span class="repost-text"> Repost by </span>' : '')."
					<a href='{$meme['poster']['link']}' title='{$meme['poster']['name']}'>
						{$meme['poster']['username']}</a>
					".($meme['poster']['you'] ? '(you)' : 
					("<button onClick='follow(this,{$meme['poster']['iduser']})'>". 
					(($meme['poster']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>"))."
				</h3>
				<p>".($meme['original'] ? // when there is an original of that post (it's a repost)
					"<a href='{$meme['original']['link']}' title='Go to original post'>Originally posted</a> by 
					<a href='{$meme['original']['poster']['link']}' title='{$meme['original']['poster']['name']}'>
						{$meme['original']['poster']['username']}</a>
					".($meme['original']['poster']['you'] ? '(you)' : 
					("<button onClick='follow(this,{$meme['original']['poster']['iduser']})'>". 
					(($meme['original']['poster']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>")) : 
					// when it's an original
					"Posted by {$meme['poster']['name']}")."</p>
			</div>
			<img src='{$meme['images']['full']}' class='meme' alt='Meme ".($meme['original'] ? 'reposted' : 'posted')." by {$meme['poster']['username']}'>";
			if(!empty($meme['caption'])) echo "<p class='meme-comment'>
			<img src='{$meme['poster']['pic']}' alt='{$meme['poster']['username']} profile picture' class='pp'/>
			".nl2br(htmlspecialchars($meme['caption']))."</p>";
			echo "<div class='meme-actions'>
				<div>
					<span class='icon-comment'></span><br>
					<span id='num-comments-{$meme['idmeme']}'>{$meme['comments-num']}</span> 
					<span id='num-comments-str-{$meme['idmeme']}'>{$meme['comments-str']}</span>
				</div>
				<div>
					<span class='icon-star-".($meme['starred'] ? "full" : "empty")."'
						onClick='star(this,{$meme['idmeme']},\"#num-stars-{$meme['idmeme']}\",\"#num-stars-str-{$meme['idmeme']}\")'></span><br>".
					($meme['stars-num'] ? "<a href='{$web}stars/{$meme['idmeme']}'>" : '') // only link the star page when there are some
					."<span id='num-stars-{$meme['idmeme']}'>{$meme['stars-num']}</span> 
					<span id='num-stars-str-{$meme['idmeme']}'>{$meme['stars-str']}</span>".
					($meme['stars-num'] ? "</a>" : '') . "
				</div>
				<div>
					<span class='icon-repost ".($meme['reposted'] ? "reposted" : ($meme['repostable'] ? "' onClick=\"expand('#repost-containter-{$meme['idmeme']}')\"" : "unrepostable'"))."'></span><br>".
					// only link the reposts page when there are some
					($meme['reposts-num'] ? "<a href='{$web}reposts/{$meme['idmeme']}'>" : '')
					."{$meme['reposts-num']} {$meme['reposts-str']}".
					($meme['reposts-num'] ? "</a>" : '')
				."</div>
			</div>";
		if($meme['repostable'])
			echo "<div class='meme-repost' id='repost-containter-{$meme['idmeme']}'>
				<h4>Repost this meme</h4>
				<button onClick=\"repost({$meme['idmeme']},'#repost-{$meme['idmeme']}')\">
					Repost
				</button>
				<div>
					<input type='text' id='repost-{$meme['idmeme']}' class='meme-repost-caption' placeholder='Add a caption... or just hit that repost button'>
				</div>
			</div>";
		if($meme['comments-num'] > count($meme['comments']))
			echo "<a href='{$meme['link']}' title='Go to post'>View all comments&hellip;</a>";
		echo "<div class='meme-comments' id='comments-{$meme['idmeme']}'>";
		if($meme['comments-num'] && $meme['comments'])
			foreach($meme['comments'] as $comment)
				echo "
				<div class='meme-comment'>
					<h4 class='meme-comment-name'>
						<div class='meme-ago'>{$comment['time']['ago']}</div>
						<a href='{$comment['commenter']['link']}' title='{$comment['commenter']['name']}'>
							<img src='{$comment['commenter']['pic']}' class='pp' alt='{$comment['commenter']['username']} profile picture' /> {$comment['commenter']['username']}
						</a>
					</h4>
					<p>".htmlspecialchars($comment['comment'])."</p>
				</div>";
		echo "</div>";
		echo "<div class='meme-add-comment'>
			<button onClick=\"comment(this, {$meme['idmeme']}, '#comment-{$meme['idmeme']}', '#comments-{$meme['idmeme']}', '#num-comments-{$meme['idmeme']}','#num-comments-str-{$meme['idmeme']}')\">
				Comment
			</button>
			<div>
				<input type='text' id='comment-{$meme['idmeme']}' class='meme-add-comment' placeholder='Add a comment...'>
			</div>
		</div>";
		echo "</div>";
	}

	function displayMemeGrid($meme) {
		// expects a $meme like those produced by the meme()['meme'] function

		echo "
		<div class='memeGridContainer'>
			<a href='{$meme['link']}' class='memeLink' title='Go to meme'>
				<div class='memeDetails'>
					<p><span class='icon-star-".($meme['starred'] ? "full" : "empty")."'></span> {$meme['stars-num']}</p>
					<p><span class='icon-comment'></span> {$meme['comments-num']}</p>
					<p><span class='icon-repost ".($meme['reposted'] ? "reposted" : ($meme['repostable'] ? '' : 'unrepostable'))."'></span> {$meme['reposts-num']}</p>
				</div>
				<img src='{$meme['images']['thumb']}' class='meme' alt='Meme ".($meme['original'] ? 'reposted' : 'posted')." by {$meme['poster']['username']}'>
			</a>
		</div>";
	}

	function displayListHeading($meme,$type) {
		// expects a $meme like those produced by the meme()['meme'] function
		// and a type of either star or repost

		echo "
		<div class='listHeadingContainer'>
			<a href='{$meme['link']}' class='meme'>
				<img src='{$meme['images']['thumb']}' alt='Meme ".($meme['original'] ? 'reposted' : 'posted')." by {$meme['poster']['username']}'>
			</a>
			<div class='details'>";

		switch ($type) {
			case 'star':
				echo "<h2>{$meme['stars-num']} {$meme['stars-str']} on this <a href='{$meme['link']}'>".($meme['original'] ? 'repost' : 'post')."</a> by <a href='{$meme['poster']['link']}'>{$meme['poster']['username']}</a></h2>";
				break;
			case 'repost':
				echo "<h2>{$meme['reposts-num']} {$meme['reposts-str']} on this ".
				($meme['original'] ?
					"<a href='{$meme['original']['link']}'>post</a>" :
					"<a href='{$meme['link']}'>post</a>"
				)
				." by ".
				($meme['original'] ?
					"<a href='{$meme['original']['poster']['link']}'>{$meme['original']['poster']['username']}</a>" :
					"<a href='{$meme['poster']['link']}'>{$meme['poster']['username']}</a>"
				)
				."</h2>";
				break;
		}
				
		echo "</div>
		</div>";
	}

	function displayUserList($event) {
		// expects an array $event containing a ['user'] and ['time']

		echo "
		<h3 class='userList'>
			<span class='follow'>".
			($event['user']['you'] ? '(you)' : 
				("<button onClick='follow(this,{$event['user']['iduser']})'>". 
				(($event['user']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>")).
			"</span>

			<a href='{$event['user']['link']}' class='pp'>
				<img src='{$event['user']['pic']}' alt='{$event['user']['username']} profile picture' class='pp'/></a>
			<a href='{$event['user']['link']}'>{$event['user']['name']}</a> <span class='ago'>{$event['time']['ago']}</span>
		</h3>
		";
	}
?>