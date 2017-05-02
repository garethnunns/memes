<?php
	require_once 'site/web.php';

	$found = true;

	if(($check = userDetailsFromUsername($_GET['username']))===false) $found = false;
	
	if($found) {
		$user = userDetailsPersonal($_SESSION['key'],$check->iduser);
		$found = $user['success'];
	}

	if($found) $title = "Followers of {$user['profile']['username']}";
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
			Followers of {$user['profile']['name']} (<a href='{$user['profile']['link']}'>{$user['profile']['username']}</a>) ".
			($user['profile']['you'] ? '(you)' : 
				("<button onClick='follow(this,{$user['profile']['iduser']})'>". 
				(($user['profile']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>"))
		."</h2>";

		$followers = followers($_SESSION['key'],$user['profile']['iduser'],0);

		if(!$followers['success'])
			echo "<p class='error'>" . ($followers['error'] ?: "There was an error getting the list of people") . "</p>";
		elseif(!$followers['num']) 
			echo "<p>No one's followed ".($user['profile']['you'] ? 'you' : $user['profile']['name'])." yet 😭</p>";
		else {
			echo "<div class='userListContainer'>";
			foreach ($followers['followers'] as $follower)
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