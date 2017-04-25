<?php require_once dirname(__FILE__).'/web.php'; ?>
<header>
	<div class="wrapper">
		<h1><a href="/"><?php echo $sitename; ?></a></h1>

<?php
	if(loggedIn()) {
		$user = userDetails($_SESSION['key']);
		// when the user is logged in these buttons will display
?>
		<div class="buttons">
			<?php echo '<a href="/'.$user->username.'" class="pp '. ($_SERVER['REQUEST_URI'] == "/{$user->username}" ? 'current': '') .'"><img src="'.$res.$user->picUri.'" alt="'.$user->username.' profile picture" /></a>'; ?>
			<a href="/starred" class="icon-star-full <?php if($_SERVER['REQUEST_URI'] == '/starred/') echo 'current' ?>" title="Starred"></a>
			<a href="/settings" class="icon-settings <?php if($_SERVER['REQUEST_URI'] == '/settings/') echo 'current' ?>" title="Settings"></a>
			<a href="/logout" class="icon-logout" title="Logout"></a>
		</div>
<?php
	}
?>
	</div>
</header>