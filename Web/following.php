<?php
	require_once 'site/web.php';
	check();

	$found = true;

	if(($check = userDetailsFromUsername($_GET['username']))===false) $found = false;
	
	if($found) {
		$user = userDetailsPersonal($_SESSION['key'],$check->iduser);
		$found = $user['success'];
	}

	if($found) $title = "Followed by {$user['profile']['username']}";
	else $title = "User not found";
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title . ' · ' . $sitename; ?></title>

		<?php include 'site/head.php'; ?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>

		<div class="wrapper">
<?php
	if($found) {
		echo "
		<h2 class='followHeading'>
			<a href='{$user['profile']['link']}' class='pp'>
				<img src='{$user['profile']['pic']}' alt='{$user['profile']['username']} profile picture' class='pp'>
			</a>
			Followed by {$user['profile']['name']} (<a href='{$user['profile']['link']}'>{$user['profile']['username']}</a>) ".
			($user['profile']['you'] ? '(you)' : 
				("<button onClick='follow(this,{$user['profile']['iduser']})'>". 
				(($user['profile']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>"))
		."</h2>";

		$following = following($_SESSION['key'],$user['profile']['iduser'],0);

		if(!$following['success'])
			echo "<p class='error'>" . ($followers['error'] ?: "There was an error getting the list of people") . "</p>";
		elseif(!$following['num']) 
			echo "<p>".($user['profile']['you'] ? "You haven't" : "{$user['profile']['name']} hasn't")." followed anyone yet 😭</p>";
		else {
			echo "<div class='userListContainer'>";
			foreach ($following['following'] as $follower)
				displayUserList($follower);
			echo '</div>';
		}
	}
	else
		echo $pageError;
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>