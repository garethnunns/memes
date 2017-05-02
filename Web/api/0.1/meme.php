<?php
	require_once '../../site/functions.php';

	echo json_encode(meme($_POST['key'],$_POST['id'],$_POST['thumb'],$_POST['full'],$_POST['limitComments']));
?>