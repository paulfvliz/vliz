
/**
  *Loop through all dropdown buttons to toggle between hiding and showing its dropdown content
  *This allows the user to have multiple dropdowns without any conflict 
  */
var dropdown = document.getElementsByClassName("dropdown-btn");
var i;

for (i = 0; i < dropdown.length; i++) {
  dropdown[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var dropdownContent = this.nextElementSibling;
    if (dropdownContent.style.display === "block") {
      dropdownContent.style.display = "none";
    } else {
      dropdownContent.style.display = "block";
    }
  });
}

var coll = document.getElementsByClassName("collapsible");

for (i = 0; i < coll.length; i++) {
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;
    if (content.style.maxHeight){
      content.style.maxHeight = null;
    } else {
      content.style.maxHeight = content.scrollHeight + "px";
    } 
    for (i = 0; i < coll.length; i++) { // find the other active tab and deactivate it and close its content
      if(this.id != coll[i].id && coll[i].classList.contains("active") ){
        console.log(coll[i].id);
        coll[i].classList.toggle("active");
        var content = coll[i].nextElementSibling;
        if (content.style.maxHeight){
          content.style.maxHeight = null;
        } else {
          content.style.maxHeight = content.scrollHeight + "px";
        }  
      }
    }


  });
}
