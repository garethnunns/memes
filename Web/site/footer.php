<footer>
<?php
	require_once dirname(__FILE__).'/web.php';
	if(loggedIn()) { 
?>
	<div class="wrapper links">
		<a href="/">Feed</a><a href="/hot">Hot</a><a href="/add">Add</a><a href="/notifications">Notifications</a><a href="/profile">Profile</a>
	</div>
<?php
	}
	else {
?>
	<div class="wrapper signup">
		<p>Why not <a href="/signup">signup for an account</a>, it's pretty cool&hellip;</p>
	</div>
<?php } ?>
</footer>