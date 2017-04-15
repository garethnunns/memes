<?php require_once dirname(__FILE__).'/functions.php'; ?>
<header>
	<div class="wrapper">
		<h1><a href="/">Meme Me</a></h1>

<?php
	if(isset($_SESSION['user'])) {
		// when the user is logged in these buttons will display
?>
		<div class="buttons">
			<a href="/logout">Logout</a>
		</div>
<?php
	}
?>
	</div>
</header>