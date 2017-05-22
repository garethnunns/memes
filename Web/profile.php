<?php
	require_once 'site/web.php';
	if(!loggedIn()) $_SESSION['key'] = 'public';

	$found = true;

	if(($username = userDetailsFromUsername($_GET['username']))===false) $found = false;

	if($found) $profile = profile($_SESSION['key'],$username->iduser,0,400);

	$found = $profile['success'];

	if($found) $title = "{$profile['user']['name']} ({$profile['user']['username']}) · $sitename";
	else $title = "User not found · $sitename";
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title ?></title>

		<script type="text/javascript">var profile = <?php echo $username->iduser ?>;</script>
		<?php 
			include 'site/head.php'; 
			if($found) {
				$link = $profile['user']['link'];
				$image = $profile['user']['pic'];
				$desc = possessive($profile['user']['firstName'])." profile on {$sitename} - {$tagline}";

				echo "
				<meta property=\"og:url\" content=\"{$link}\">
				<meta property=\"og:title\" content=\"{$title}\">
				<meta property=\"og:description\" content=\"{$desc}\">
				<meta property=\"og:image\" content=\"{$image}\">

				<meta property=\"og:type\" content=\"profile\">
				<meta property=\"profile:first_name\" content=\"{$profile['user']['firstName']}\">
				<meta property=\"profile:last_name\" content=\"{$profile['user']['surname']}\">

				<meta property=\"twitter:title\" content=\"{$title}\">
				<meta property=\"twitter:description\" content=\"{$desc}\">
				<meta property=\"twitter:image\" content=\"{$image}\">

				<link rel=\"canonical\" href=\"{$link}\"/>
				<meta name=\"description\" content=\"{$desc}\">";
			}
		?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>

		<div class="wrapper">
<?php
	if($found) {
		echo "
		<div class='profile'>
			<img src='{$profile['user']['pic']}' class='pp' alt='{$profile['user']['username']} profile picture'>
			<h1>{$profile['user']['username']}</h1>
			<p>{$profile['user']['name']}".($profile['user']['you'] ? ' (you)' : '')."</p>
			".($profile['user']['you'] ? '<a href="/settings">Edit your profile</a>' : 
				("<button onClick='follow(this,{$profile['user']['iduser']},\"#num-followers\",\"#num-followers-str\")'>". 
				(($profile['user']['isFollowing']) ? 'Unfollow' : 'Follow') . "</button>"))."
			<div class='stats'>
				<div>
					<div class='number'>{$profile['stats']['posts']}</div>
					{$profile['stats']['posts-str']}
				</div>
				<div>
					<div class='number' id='num-followers'>{$profile['stats']['followers']}</div>".
					($profile['stats']['followers'] ? // only show the followers link if there are some
						"<a href='{$profile['user']['username']}/followers'>" : '')
					."<span id='num-followers-str'>{$profile['stats']['followers-str']}</span>".
					($profile['stats']['followers'] ? "</a>" : '')."
				</div>
				<div>
					<div class='number'>{$profile['stats']['following']}</div>".
					($profile['stats']['following'] ? // only show the followers link if there are some
						"<a href='{$profile['user']['username']}/following'>" : '')
					."{$profile['stats']['following-str']}".
					($profile['stats']['following'] ? "</a>" : '')."
				</div>
				<div>
					<div class='number'>{$profile['stats']['stars']}</div>
					{$profile['stats']['stars-str']}
				</div>
			</div>
		</div>";

		if(!count($profile['memes']))
			echo "<p class='center'><i>This user hasn't posted any memes at the moment&hellip;</p>";
		else {
			echo "<div class='memeGrid'>";
			foreach ($profile['memes'] as $meme) 
				displayMemeGrid($meme);
			echo "</div>";
		}
	}
	else {
		echo $pageError;
		if(isset($profile['error'])) echo "<p class='error'>{$profile['error']}</p>";
	}
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>