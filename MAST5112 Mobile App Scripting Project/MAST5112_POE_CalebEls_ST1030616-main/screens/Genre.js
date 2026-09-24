import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, SafeAreaView, Dimensions } from 'react-native';
import { Header } from 'react-native-elements';

const { width, height } = Dimensions.get('window');

export default function Genre({ route, navigation }) {
  const { books } = route.params || {};

  const [genreCounts, setGenreCounts] = useState({
    FICTION: 0,
    'NON-FICTION': 0,
    ROMANCE: 0,
    ACTION: 0,
    COMEDY: 0,
  });

  const [genresData, setGenresData] = useState([]);

  useEffect(() => {
    updateGenreCounts();
  }, [books]);

  useEffect(() => {
    updateGenresData();
  }, [genreCounts]);

  const updateGenreCounts = () => {
    if (!books) {
      // Handle the case where books is undefined
      return;
    }

    const updatedGenreCounts = {
      FICTION: 0,
      'NON-FICTION': 0,
      ROMANCE: 0,
      ACTION: 0,
      COMEDY: 0,
    };

    books.forEach((book) => {
      const genre = book.genre ? book.genre.toUpperCase() : '';
      updatedGenreCounts[genre] += 1;
    });

    setGenreCounts(updatedGenreCounts);
  };

  const updateGenresData = () => {
    const updatedGenresData = Object.keys(genreCounts).map((genre) => ({
      genre,
      numBooks: genreCounts[genre],
    }));

    setGenresData(updatedGenresData);
  };

  return (
    <SafeAreaView style={styles.container}>
      <Header
        containerStyle={{ marginTop: -150 }}
        backgroundColor="#21c46b"
        leftComponent={
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <Image
              source={require('./assets/LOGO.png')}
              style={styles.Logo}
            />
            <Text style={styles.title}>GENRE</Text>
          </View>
        }
      />
      <Text></Text>
      <Text></Text>

      <View style={styles.columnContainer}>
        <View style={styles.genresColumn}>
          <Text style={styles.title2}>GENRES:</Text>
          {genresData.map((genreInfo, index) => (
            <View key={index} style={styles.genreContainer}>
              <Text style={styles.text}>{genreInfo.genre}:</Text>
            </View>
          ))}
        </View>

        <View style={styles.booksColumn}>
          <Text style={styles.title2}>No. OF BOOKS:</Text>
          {genresData.map((genreInfo, index) => (
            <View key={index} style={styles.genreContainer}>
              <Text style={styles.text}>{genreInfo.numBooks}</Text>
            </View>
          ))}
        </View>
      </View>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <Text></Text>
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <TouchableOpacity onPress={() => navigation.navigate('Home', { books })}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HOME</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => navigation.navigate('AddBook')}>
          <View style={styles.button}>
            <Text style={styles.btnText}>ADD BOOK</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => navigation.navigate('History', { books })}>
          <View style={styles.button}>
            <Text style={styles.btnText}>HISTORY</Text>
          </View>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    flex: 1,
    backgroundColor: '#217bc4',
  },
  background: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: width,
    height: height,
  },
  title: {
    fontSize: 36,
    color: '#1E78CB',
    marginBottom: 24,
    textAlign: 'center',
  },
  btnText: {
    fontSize: 20,
    color: '#217bc4',
    fontWeight: '800',
  },
  button: {
    borderColor: '1E78CB',
    padding: 10,
    backgroundColor: '#21c46b',
    alignItems: 'center',
    borderRadius: 30,
    height: 60,
    width: 150,
  },
  title2: {
    fontSize: 30,
    color: '#6aec8e',
    marginBottom: 24,
    textAlign: 'center',
  },
  text: {
    fontSize: 20,
    color: '#6aec8e',
    textAlign: 'center',
  },
  Logo: {
    width: 200,
    height: 210,
    marginRight: 10,
  },
  columnContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  genresColumn: {
    flex: 1,
    alignItems: 'center',
  },
  booksColumn: {
    flex: 1,
    alignItems: 'center',
  },
  genreContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#217bc4',
    padding: 10,
    borderRadius: 8,
    marginVertical: 8,
    borderWidth: 5,
    borderColor: '#6aec8e',
  },
});
//IIE, 2023. 


//References: 

//IIE, 2023. MOBILE APP SCRIPTING MAST5112 [Module Manual]. The Independent Institute of Education: Unpublished 

//Figma.2023. [Online] Available at: https://www.figma.com/. Accessed [07/09/2023]  

//Canva 2023.[Online] Available at: https://www.canva.com/. Accessed [07/08/2023] 

//Fandom.2023.[Online] Available at: https://adventuretimesuperfans.fandom.com/wiki/Shelby . Accessed [07/08/2023] 

//Stack Overflow. 2023. [Online] Available at:   https://stackoverflow.com/questions/55382042/how-to-show-amount-of-times-user-has-logged-in-using-asyncstorage. Accessed [24/11/2023]

//Bobbyhadz.com. 2023. How to use the forEach() method in React, 2023 [Online] Available at:   https://bobbyhadz.com/blog/react-foreach. Accessed [24/11/2023]

//npmjs. 2021. react-native-table-component, 2021 [Online] Available at:   https://www.npmjs.com/package/react-native-table-component. Accessed [22/11/2023]

//Geeks for Geeks. 2023. How to Create A Simple Counter App using React Native ?, 2023 [Online] Available at:   https://www.geeksforgeeks.org/how-to-create-a-simple-counter-app-using-react-native/. Accessed [24/11/2023]