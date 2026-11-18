  document.addEventListener('click', function (e) {
      var link = e.target.closest('#sidenav a[href]:not(.disabled)');
      if (!link || link.getAttribute('href') === '#') return;
      e.preventDefault();
      navigate(link.href, true);
  });

  window.addEventListener('popstate', function () {
      navigate(location.href, false);
  });

  async function navigate(url, push) {
      if (typeof window.pageCleanup === 'function') {
          window.pageCleanup();
          window.pageCleanup = null;
      }

      var response = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
      if (!response.ok) {
          location.href = url;
          return;
      }

      var doc = new DOMParser().parseFromString(await response.text(), 'text/html');
      document.getElementById('main-content').innerHTML = doc.getElementById('main-content').innerHTML;
      var holder = document.getElementById('page-scripts');
      holder.innerHTML = '';
      doc.querySelectorAll('#page-scripts script').forEach(function (old) {
          var s = document.createElement('script');
          if (old.src) { s.src = old.src; } else { s.textContent = old.textContent; }
          holder.appendChild(s);
      });

      var active = doc.querySelector('#sidenav a.active');
      document.querySelectorAll('#sidenav a').forEach(function (a) {
          a.classList.toggle('active', !!active && a.getAttribute('href') === active.getAttribute('href'));
      });

      document.title = doc.title;
      if (push) history.pushState({}, '', url);
  }