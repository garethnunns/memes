<footer>
<?php
	require_once dirname(__FILE__).'/web.php';
	if(loggedIn()) { 
		$user = userDetails($_SESSION['key']);
?>
	<div class="wrapper links">
		<a href="/" class="icon-feed <?php if($_SERVER['REQUEST_URI'] == '/') echo 'current' ?>"></a>
		<a href="/hot" class="icon-hot <?php if($_SERVER['REQUEST_URI'] == '/hot/') echo 'current' ?>"></a>
		<a href="/add" class="icon-add <?php if($_SERVER['REQUEST_URI'] == '/add/') echo 'current' ?>"></a>
		<a href="/notifications" class="icon-notifications <?php if($_SERVER['REQUEST_URI'] == '/notifications/') echo 'current' ?>"></a>
		<?php echo '<a href="/'.$user->username.'" class="pp"><img src="'.$res.$user->picUri.'" alt="'.$user->username.' profile picture" /></a>'; ?>
	</div>
<?php
	}
	else {
?>
	<div class="wrapper signup">
		<p>Why not <a href="/signup">signup for an account</a>,<wbr> they're pretty cool&hellip;</p>
	</div>
<?php } ?>
</footer>