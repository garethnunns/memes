<?php require_once dirname(__FILE__).'/web.php'; ?>
<header>
	<div class="wrapper">
		<h1><a href="/">Meme Me</a></h1>

<?php
	if(loggedIn()) {
		$user = userDetails($_SESSION['key']);
		// when the user is logged in these buttons will display
?>
		<div class="buttons">
			<?php echo '<a href="/'.$user->username.'" class="pp"><img src="'.$res.$user->picUri.'" alt="'.$user->username.' profile picture" /></a>'; ?>
			<a href="/starred" class="icon-star-full" title="Starred"></a>
			<a href="/settings" class="icon-settings" title="Settings"></a>
			<a href="/logout" class="icon-exit" title="Logout"></a>
		</div>
<?php
	}
?>
	</div>
</header>