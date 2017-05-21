<?php
	require_once '../site/web.php';
	check();

	$nots = notifications($_SESSION['key'],$_POST['page'],150);

	if($nots['success'] && count($nots['notifications'])) {
		foreach ($nots['notifications'] as $not) {
			echo '<div class="notification '.($not['unread'] ? 'unread' : 'read').'">';

			$user = "<a href='{$not['user']['link']}' class='pp'>
				<img src='{$not['user']['pic']}' alt='{$not['user']['username']} profile picture' class='pp'/></a> ";

			switch ($not['type']) {
				case 'follow':
					echo "<div class='follow'>".($not['user']['you'] ? '(you)' : 
						("<button onClick='follow(this,{$not['user']['iduser']})'>". 
						(($not['user']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>")).
					"</div>
					$user
					<p><a href='{$not['user']['link']}'>{$not['user']['name']}</a> "." followed you 
					<span class='ago'>{$not['time']['ago']}</span></p>";
					break;
				
				default:
					echo "
					<a href='{$not['meme']['link']}' class='meme'>
						<img src='{$not['meme']['images']['thumb']}' alt='Your post'>
					</a>
					$user
					<p><a href='{$not['user']['link']}'>{$not['user']['name']}</a> ".
					($not['type']=='star' ? 'starred' : 
						($not['type']=='comment' ? 'commented' : 
							($not['type']=='repost' ? 'reposted' : $not['type'].'ed'))).
					" your <a href='{$not['meme']['link']}' title='Go to meme'>".
					($not['meme']['original'] ? 'repost' : 'post')."</a> 
					<span class='ago'>{$not['time']['ago']}</span></p>";
					break;
			}
			echo '</div>';
		}
	}
?>